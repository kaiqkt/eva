# Plano: Buildar e versionar imagens de apps via Woodpecker CI no Eva

## Contexto

Eva é um serviço estilo PaaS: provisiona um repositório Forgejo por Application (`CreateApplicationService.kt:29`).
A próxima capacidade é o laço central do PaaS — **transformar o código-fonte de um app em uma imagem de container versionada**.
Hoje o domínio para na `url` do repositório: `Application` não tem noção de build/imagem/versão, não existe infraestrutura
de jobs, e não existe nenhum código de execução de container/registry.

**Mudança de abordagem vs. planos anteriores:** em vez de o Eva orquestrar o build (poller + worker + daemon de build
próprio), o Eva **delega o build a um CI (Woodpecker)** e fica como *orquestrador + fonte da verdade dos metadados*. O CI
faz o trabalho pesado e assíncrono (clone, build, push); o Eva dispara, recebe o status de volta e guarda o registro do
`Build` (status, versão, imagem, link) para o usuário visualizar.

### Por que Woodpecker (e não Eva orquestrando)
- **Menos código no Eva:** some o poller `@Scheduled`, o worker, o claim `SELECT … FOR UPDATE SKIP LOCKED`, o clone JGit e
  o adapter docker-java. O CI já resolve fila, concorrência, retries, timeout e logs — battle-tested.
- **Zero dependência JVM nova:** o Eva só fala HTTP com a API do Woodpecker (reusa `RestClient` + Jackson, já presentes).
  Nenhum binário de build (nixpacks/kaniko) precisa existir no host do Eva — eles vivem nas imagens dos steps do CI.
- **Build efêmero por job:** sem daemon de build privilegiado long-running gerenciado pelo Eva.
- **Controle dinâmico central:** o Eva decide, por app e por build, o registry/tag/estratégia (ver decisão da fonte de pipeline).
- **"+ o CI":** o usuário ganha os dois — os metadados resumidos na API do Eva **e** a UI do Woodpecker com logs/pipeline detalhado.

### Decisões (confirmadas com o usuário)
- **Mecanismo:** **Woodpecker CI** (server + agent) plugado no Forgejo como forge.
- **Engine de build (dentro do step):** **kaniko** (daemonless — builda e dá push sem daemon Docker).
- **Entrada de build:** Dockerfile primeiro; apps **sem Dockerfile** usam **Nixpacks só como gerador de Dockerfile**
  (`nixpacks build . --out .` → `.nixpacks/Dockerfile`), que o kaniko então builda. Detecção feita **dentro do pipeline**.
- **Registry:** registry OCI embutido do Forgejo 10 (`forgejo:3000/<owner>/<app>:v{n}`) — servido por HTTP, kaniko com
  `--insecure --skip-tls-verify`.
- **Gatilho:** `POST /applications/{slug}/builds` manual → `202 Accepted`; o Eva dispara o pipeline via API do Woodpecker.
- **Status:** o pipeline **faz callback** para o Eva (push) no fim; o Eva persiste o `Build` como fonte da verdade.
- **Versão:** o Eva é dono da numeração `v{n}` (sequencial por app) — o CI não decide isso.

### Decisão em aberto: de onde vem o YAML do pipeline
Duas formas de o Woodpecker obter a config do pipeline. **Recomendado: External Configuration API.**

| Opção | Como | Prós | Contras |
|-------|------|------|---------|
| **A. External Configuration API** *(recomendada)* | `WOODPECKER_CONFIG_SERVICE_ENDPOINT` → endpoint no Eva que **gera o YAML na hora** por build | Pipeline **não vive no repo do usuário** (sem commit, sem conflito com o push do usuário, sem clutter); Eva controla 100% (tag/registry/estratégia dinâmicos) | Eva expõe 1 endpoint HTTP que **precisa verificar a assinatura** ed25519 do Woodpecker (JDK 21 tem Ed25519 nativo) |
| **B. `.woodpecker.yml` templado** | Eva commita o arquivo no repo ao provisionar; valores dinâmicos via secrets/vars | Sem endpoint novo | CI-config mora no repo do usuário (editável/apagável); Eva precisa commitar (novo `contents` API do Forgejo) e lidar com ordem vs. o `git push` inicial do usuário |

O restante do plano assume a **Opção A**; a seção 3 marca o que muda se optar pela B.

### Ressalva honesta (afeta o design)
- **Nova infra:** Woodpecker = server + agent + um **OAuth2 app no Forgejo** (setup manual único) + um **token de API do
  Woodpecker** para o Eva (como o token do Forgejo hoje). Mais peças para operar.
- **Fronteira de confiança move para o agent:** o build de código não confiável agora roda no **agent** do Woodpecker.
  O agent executa steps como containers via backend Docker → precisa de um **dind dedicado** (nunca o socket do host).
  A superfície de "código arbitrário no build" não some, só muda de dono. Ver seção Segurança.
- **Novo endpoint assinado:** a Opção A adiciona superfície HTTP no Eva que precisa de verificação de assinatura correta.

---

## Arquitetura

```
POST /applications ──▶ CreateApplicationService
                         ├─ git.create (Forgejo repo)                        [já existe]
                         └─ woodpecker.enableRepo(slug)  ── habilita o repo no Woodpecker  [novo]

POST /applications/{slug}/builds ──▶ TriggerBuildService
                         ├─ resolve app (404 se ausente)
                         ├─ aloca number n, salva Build(QUEUED, number=n)
                         └─ woodpecker.triggerPipeline(repo, branch, vars)   ── 202 {number, status:QUEUED}
                                                       │
   Woodpecker server ──[Opção A: GET config]──▶ POST /internal/woodpecker/config (Eva gera o YAML) [assinado]
                                                       │
   Woodpecker agent (dind dedicado) roda os steps:
     1. detect  : [ -f Dockerfile ] ? dockerfile : nixpacks build . --out .
     2. kaniko  : build + push  ──▶  forgejo:3000/<owner>/<app>:v{n}   (--insecure)
     3. callback: POST /internal/builds/{ref}/status  ──▶ Eva
                                                       │
   ReportBuildStatusService ──▶ atualiza Build(SUCCESS/FAILED, imageRef, digest, logsUrl)
                                                       │
   GET /applications/{slug}/builds/{n} ──▶ usuário visualiza status/versão/imagem/link
```

O layering segue a regra hexagonal do repo (adapters dependem de `application`/`domain`, nunca o contrário). A nova
integração outbound copia a divisão `adapter/outbound/forgejo/{adapter,client,config}` no novo pacote `woodpecker/`.

---

## Mudanças por camada

### 1. Domínio (`domain/model/`)
- `Build.kt` — data class. **Diverge de `Application`/`Project` por carregar `id`** (nulo antes do save) e um `number`
  sequencial por app, porque um build é endereçado individualmente e correlacionado através da fronteira do CI:
  ```
  data class Build(
      id: String?,               // ULID surrogate (entity), null pré-save
      applicationId: String,
      number: Int,               // sequencial por app; versão = "v$number"; chave de negócio na REST
      ref: String,               // branch/commit buildado
      status: BuildStatus,
      imageRef: String?,         // forgejo:3000/<owner>/<app>:v{number}
      imageDigest: String?,
      strategy: BuildStrategy?,  // reportado pelo pipeline no callback (informativo)
      ciPipelineNumber: Long?,   // número do pipeline no Woodpecker (reconcile + deep link)
      logsUrl: String?,          // deep link para o pipeline no Woodpecker
      createdAt: Instant, startedAt: Instant?, finishedAt: Instant?
  )
  ```
- `BuildStatus.kt` — enum `QUEUED, RUNNING, SUCCESS, FAILED, CANCELED`.
- `BuildStrategy.kt` — enum `DOCKERFILE, NIXPACKS` (só rótulo do que o pipeline usou).
- **Removido do plano antigo:** `BuildStrategyResolver` não existe no Eva — a detecção Dockerfile-vs-Nixpacks é feita
  **dentro do pipeline** (shell no step `detect`). Eva só registra a estratégia reportada.

### 2. Ports da aplicação & casos de uso
Inbound (`port/inbound/`):
- `TriggerBuildUseCase` — `trigger(appSlug, ref?): Build`.
- `GetBuildUseCase` — `get(appSlug, number): Build`.
- `ListBuildsUseCase` — `list(appSlug): List<Build>`.
- `ReportBuildStatusUseCase` — `report(ref, StatusUpdate)` (chamado pelo callback do CI).

Outbound (`port/outbound/`):
- `BuildRepositoryPort` — `save`, `findByApplicationIdAndNumber`, `findByApplicationId`, `nextNumber(applicationId): Int`.
- `CiPort` — abstrai o Woodpecker por completo:
  `enableRepo(appSlug)`, `triggerPipeline(appSlug, branch, vars): CiPipelineRef`, `pipelineDeepLink(...)`,
  `getPipeline(appSlug, number): CiPipelineStatus` (para reconcile).

Casos de uso (`application/usecase/`, espelham `CreateApplicationService`):
- `TriggerBuildService` — resolve o app pelo slug (404, reusa `ResourceNotFoundException`), aloca `number`, salva
  `Build(QUEUED)`, chama `CiPort.triggerPipeline` com as variáveis do build (tag `v{n}`, registry, callback token),
  guarda `ciPipelineNumber`, retorna.
- `ReportBuildStatusService` — recebe o status do callback → atualiza a linha (`RUNNING`/`SUCCESS`/`FAILED`, `imageRef`,
  `imageDigest`, `strategy`, `logsUrl`, timestamps). É a **única** via de escrita de estado terminal.
- **Sem `RunBuildService`** — quem "roda" o build é o CI, não o Eva.

### 3. Adapters inbound (`adapter/inbound/web/`)
- `controller/BuildController.kt` — espelha `ApplicationController` (fino, delega ao inbound port):
  - `POST /applications/{slug}/builds` → `202` `BuildResponse` (request `TriggerBuildRequest(ref?)`).
  - `GET /applications/{slug}/builds/{number}` → `BuildResponse` (status/versão/imagem/link).
  - `GET /applications/{slug}/builds` → lista/histórico.
- `controller/internal/BuildCallbackController.kt` — `POST /internal/builds/{ref}/status` recebido do pipeline.
  Autenticado por **token por-build** (o Eva gera no trigger, passa como secret do pipeline, o step ecoa no header).
- `controller/internal/WoodpeckerConfigController.kt` *(Opção A)* — `POST /internal/woodpecker/config`. **Verifica a
  assinatura ed25519** do Woodpecker (chave pública em `GET {woodpecker}/api/signature/public-key`; JDK 21:
  `Signature.getInstance("Ed25519")`) antes de gerar e retornar o YAML do pipeline. **Rejeita 403 sem assinatura válida.**
  - *Se optar pela Opção B:* este controller não existe; em vez disso, `CreateApplicationService`/`CiPort` commita um
    `.woodpecker.yml` no repo via novo método `contents` no `ForgejoHttpClient`.
- `ErrorType`/`GlobalExceptionHandler`: adicionar `ErrorType.UNAUTHORIZED → 401/403` para os endpoints internos.
- **Sem scheduler/poller** (`@Scheduled`, `@EnableScheduling`) — não há worker no Eva.

### 4. Adapters outbound — novo pacote `adapter/outbound/woodpecker/` (copia o formato de `forgejo/`)
- `adapter/WoodpeckerAdapter.kt : CiPort` — traduz domínio ↔ chamadas do client.
- `client/WoodpeckerHttpClient.kt` — `RestClient` com `@Qualifier("woodpecker-rest-client")` e header
  `Authorization: Bearer <token>`, mesmo padrão do `ForgejoHttpClient` (incl. `onStatus(isError)` → `WoodpeckerException`):
  - habilitar repo (enable) e/ou `GET /api/repos/lookup/{owner}/{app}` para achar o `repoId` no trigger (evita guardar id).
  - `POST /api/repos/{repoId}/pipelines` `{branch, variables}` → dispara e retorna o número do pipeline.
  - `GET /api/repos/{repoId}/pipelines/{number}` → status (reconcile).
  - setar secrets do repo (registry user/senha, callback token) via API de secrets, no enable.
- `config/WoodpeckerProperties.kt` — `@ConfigurationProperties(prefix="woodpecker")`: `baseUrl`, `token`,
  `connectTimeout`, `readTimeout`, `callbackBaseUrl` (URL do Eva que o pipeline chama), `registry` (host/user/senha),
  `configSigningEnabled`. Auto-registrado pelo `@ConfigurationPropertiesScan` (`EvaApplication.kt:8`), igual a `ForgejoProperties`.
- `config/WoodpeckerConfig.kt` — bean `RestClient` `woodpecker-rest-client`, espelha `ForgejoConfig.kt`.
- **Nenhum pacote `docker/` nem `source/`** (sem docker-java, sem JGit) — o clone e o build são do CI.

### 5. Persistência (copia o conjunto de persistência de Application)
`entity/BuildEntity.kt`, `mapper/BuildMapper.kt`, `repository/BuildJpaRepository.kt`, `adapter/BuildRepositoryAdapter.kt`.
- `@Id` ULID por default, igual a `ApplicationEntity.kt:24`.
- `BuildJpaRepository`: `findByApplicationIdAndNumber`, `findByApplicationIdOrderByNumberDesc`,
  e alocação de `number` via `SELECT COALESCE(MAX(number),0)+1`.
- **Alocação de `number` segura:** constraint única `(application_id, number)`; em corrida, a segunda inserção viola a
  única → **retry** (recomputa MAX+1). Não precisa de SKIP LOCKED (não há poller).

### 6. Migration
`src/main/resources/db/migration/V2__create_builds.sql` (estilo do `V1`, Flyway):
- tabela `builds`: `id VARCHAR(26)` PK, `application_id VARCHAR(26)` FK → `applications.id`, `number INT NOT NULL`,
  `ref VARCHAR(255)`, `status VARCHAR(20)`, `strategy VARCHAR(20)`, `image_ref VARCHAR(255)`,
  `image_digest VARCHAR(255)`, `ci_pipeline_number BIGINT`, `logs_url VARCHAR(255)`,
  `created_at/started_at/finished_at TIMESTAMP`.
- `CONSTRAINT uk_builds_app_number UNIQUE (application_id, number)`, índice em `(application_id, status)`.
- `ddl-auto: validate` (application.yml) exige que a migration bata com a entity.

### 7. Dependências (`gradle/libs.versions.toml` + `build.gradle.kts`)
- **Nenhuma nova.** HTTP = `spring-boot-starter-web`/`RestClient`; JSON = `jackson-module-kotlin`; ULID = `ulid-creator`;
  verificação ed25519 = **JDK 21 nativo** (`java.security`, sem lib). Tudo já no projeto.
- **kaniko** e **nixpacks** **não** são deps do Eva — são imagens/binários usados **nos steps do pipeline** do Woodpecker.

### 8. Infra (`docker-compose.yml`)
- **BuildKit removido** (feito neste passo; `buildkitd.toml` deletado).
- **A adicionar** (documentado aqui; aplicar quando for implementar):
  - `woodpecker-server` — UI + API + orquestração. Precisa de um **OAuth2 app no Forgejo** (client id/secret via env),
    `WOODPECKER_FORGEJO=true` + `WOODPECKER_FORGEJO_URL=http://forgejo:3000`, um admin, e (Opção A)
    `WOODPECKER_CONFIG_SERVICE_ENDPOINT=http://eva:8080/internal/woodpecker/config`. Porta UI exposta ao host.
  - `woodpecker-agent` — executa os pipelines. Backend Docker → aponta para um **dind dedicado** (não o socket do host).
    `WOODPECKER_MAX_WORKFLOWS`, timeout por pipeline, limites de CPU/mem no dind.
  - `dind` dedicado do agent (`docker:dind`, `privileged`) — isolado do host; inicia com `--insecure-registry forgejo:3000`
    para o kaniko dar push por HTTP. (É o dind que o plano anterior descrevia, agora a serviço do agent, não do Eva.)
  - Mesma rede default `eva_default` para todos alcançarem `forgejo:3000` e o Eva.
- Config `woodpecker` (`application-local.yml` + env em prod, relaxed binding `WOODPECKER_*` igual ao Forgejo):
  `base-url: http://localhost:8000`, `token`, `callback-base-url: http://eva:8080`, `registry.host: forgejo:3000`,
  `registry.user/password: eva`.
- (Follow-up, não v1) `Dockerfile` do Eva + serviço `eva` no compose quando o próprio Eva for containerizado.

### 9. Fluxo de provisionamento (estende o existente)
`CreateApplicationService.create` ganha um passo após `git.create` (`CreateApplicationService.kt:29`):
- `ciPort.enableRepo(slug)` — habilita o repo no Woodpecker e seta os secrets (registry, callback token).
- Idempotência/rollback: se o enable falhar, decidir se aborta a criação ou marca o app como "CI pendente" (recomendado:
  falhar a criação para não deixar app meio-provisionado, já que hoje `git.create` também é best-effort síncrono).

### 10. Propagação de status
Fonte da verdade = a linha `Build` no Postgres. Entrega:
- **Callback (primário):** step final do pipeline com `when: status: [success, failure]` faz
  `POST /internal/builds/{ref}/status` com `{status, imageRef, imageDigest, strategy, logsUrl}`. Push, quase real-time.
- **Reconcile (rede de segurança):** se o callback se perder, o `Build` fica preso em `QUEUED`/`RUNNING`. Um
  `@Scheduled` leve (único uso de scheduling) varre builds não-terminais antigos e consulta
  `CiPort.getPipeline` para reconciliar → `SUCCESS`/`FAILED`/`CANCELED`. Alternativa a curto prazo: `GET` sob demanda
  reconcilia no read.
- **Logs/links:** `logsUrl` = deep link para o pipeline no Woodpecker (logs ao vivo lá); `imageRef` = tag da versão;
  opcional: URL da página de package do Forgejo. Tudo devolvido no `BuildResponse` para o frontend do Eva.
- **Polling do cliente:** `GET /applications/{slug}/builds/{n}` até estado terminal. Sem SSE no v1 (o Woodpecker já
  entrega stream de logs na própria UI; SSE no Eva vira follow-up se necessário).

---

## Segurança (buildar código não confiável do usuário — ler antes de implementar)
- O build roda `Dockerfile`/source arbitrário do usuário = **execução de código arbitrário**, agora no **agent do Woodpecker**:
  - Agent com **dind dedicado**, nunca o socket do host; nunca montar o socket do host num step.
  - **Limites por pipeline:** timeout rígido do Woodpecker + CPU/mem/pids no dind; limitar workflows concorrentes.
  - kaniko roda em container (sem daemon privilegiado de build); ainda assim tratar o agent como zona hostil.
  - **Credenciais do registry** via **secrets do Woodpecker**, injetadas só no step do kaniko, nunca em log/build-arg.
  - Restringir saída de rede do build onde possível; allowlist de imagens base (supply-chain via Nixpacks + `FROM`) depois.
- **Superfície HTTP nova do Eva:**
  - `/internal/woodpecker/config` (Opção A) **deve** verificar a assinatura ed25519 do Woodpecker — sem isso, qualquer um
    injeta pipeline. Rejeitar com 403 se ausente/inválida.
  - `/internal/builds/{ref}/status` autenticado por token por-build (gerado no trigger, secret no pipeline). Validar que o
    `ref` bate com o token; ignorar transições para builds já terminais.
- Endurecimento multi-tenant (agent efêmero por build, gVisor/kata) é passo posterior; registrar o risco do dind
  compartilhado do agent agora.

---

## Verificação (ponta a ponta)
1. `docker compose up` — `postgres`, `forgejo`, `forgejo-postgres`, `woodpecker-server`, `woodpecker-agent` e o `dind`
   do agent sobem saudáveis. OAuth2 app do Forgejo configurado; token de API do Woodpecker no config do Eva.
2. Fluxo existente + novo provisionamento: `POST /projects` → `POST /applications` → confirma repo Forgejo criado **e**
   repo habilitado no Woodpecker (aparece na UI/`GET /api/repos/lookup/...`).
3. Semear o repo de duas formas: (a) app **com** `Dockerfile`; (b) app **sem** Dockerfile (Node/Go) para exercitar o Nixpacks.
4. `POST /applications/{slug}/builds` → `202 {number, status:QUEUED}`; o pipeline aparece rodando na UI do Woodpecker.
5. Opção A: confirmar que o Woodpecker chamou `/internal/woodpecker/config` e que assinatura inválida é rejeitada com 403.
6. Polling em `GET /applications/{slug}/builds/{number}` → `QUEUED → RUNNING → SUCCESS`, com `imageRef`, `imageDigest`,
   `strategy` e `logsUrl` preenchidos via callback.
7. Confirmar a imagem no registry: `curl http://localhost:3000/v2/eva/<app>/tags/list` deve listar `v{n}`; `docker pull`
   da tag e rodar.
8. Negativo: repo com Dockerfile quebrado → pipeline falha → callback grava `FAILED` na linha (sem HTTP 500 no Eva).
9. Reconcile: matar o agent no meio de um build → o `@Scheduled` de reconcile tira a linha de `RUNNING` conforme o estado
   real do Woodpecker (não fica presa).
10. Testes (deps já presentes — MockK, Testcontainers-postgres; hoje zero arquivos de teste): unit de `TriggerBuildService`
    e `ReportBuildStatusService` com MockK (CiPort mockado); integração Testcontainers para a alocação de `number`
    (corrida → unique violation → retry); teste da verificação de assinatura ed25519 do config endpoint.
