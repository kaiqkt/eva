# Eva Server

Spring Boot (Kotlin) seguindo **Arquitetura Hexagonal (Ports & Adapters)**.

## Conceitos

O núcleo de negócio fica isolado — não conhece banco, HTTP ou fila. O mundo externo só fala com o núcleo através de **portas** (interfaces), e **adaptadores** implementam essas portas.

- **Domínio / Core** — regras de negócio puras. Zero dependência de framework.
  (Hoje `application/usecase` ainda usa `@Service` do Spring para registrar os beans —
  concessão pragmática consciente; o `domain/` em si é livre de framework.)
- **Portas (ports)** — interfaces. Contrato entre o núcleo e o mundo externo.
  - **Inbound / driving** — quem chama a aplicação (ex: `CreateXUseCase`). Ator externo → app.
  - **Outbound / driven** — quem a aplicação chama (ex: `XRepositoryPort`). App → infra.
- **Adaptadores (adapters)** — implementação concreta das portas.
  - **Inbound** — controllers REST.
  - **Outbound** — repositório JPA, clientes HTTP.

**Regra de dependência:** as setas apontam sempre para dentro. A infra depende do domínio, nunca o inverso. O núcleo define a interface, a infra implementa.

> Nota: usamos `inbound`/`outbound` nos nomes de pacote em vez de `in`/`out` — `in` e `out` são palavras reservadas do Kotlin e não podem ser nomes de pacote.

## Estrutura de pacotes

```
dev.kaiqkt.eva
├── EvaApplication.kt              # main + @SpringBootApplication (component scan)
│
├── domain/                        # núcleo puro
│   ├── model/                     # entidades, value objects e suas factories
│   └── exception/                 # falhas de negócio (DomainException + ErrorType)
│
├── application/                   # orquestração dos casos de uso
│   ├── port/
│   │   ├── inbound/               # driving ports — Create/Find/Update/Delete...UseCase
│   │   └── outbound/              # driven ports — ...RepositoryPort, ...Port (+ suas exceções)
│   └── usecase/                   # implementa port.inbound, usa port.outbound — ...UseCaseImpl
│
└── adapter/                       # tudo que toca o mundo externo
    ├── inbound/
    │   └── web/
    │       ├── controller/        # controllers REST (finos: validam entrada, delegam ao use case)
    │       ├── request/           # DTOs de entrada + RequestConstraints (limites compartilhados)
    │       ├── response/          # DTOs de saída — domain nunca vaza pra borda
    │       ├── advice/            # @RestControllerAdvice — traduz exceção em ProblemDetail
    │       └── config/            # OpenApiConfig (springdoc)
    └── outbound/
        ├── persistence/
        │   ├── ProjectPersistenceAdapter.kt      # implementa ProjectRepositoryPort
        │   ├── ApplicationPersistenceAdapter.kt  # implementa ApplicationRepositoryPort
        │   ├── entity/            # @Entity JPA (+ PersistableEntity)
        │   ├── mapper/            # entity <-> domain
        │   └── jpa/               # interfaces Spring Data (transporte)
        └── forgejo/
            ├── ForgejoCodeHostingAdapter.kt      # implementa CodeHostingPort
            ├── client/            # ForgejoHttpClient + request/ + response/ + ForgejoException
            └── config/            # RestClient e @ConfigurationProperties
```

**Convenção de nomes de pacote: sempre no singular** (`controller`, `request`, `response`,
`client`, `mapper`, `config`), seguindo o padrão Java/Kotlin.

**Regra dos pacotes de adapter outbound: a raiz do pacote é a porta implementada.**
A classe que implementa o port fica solta na raiz de `persistence/`, `forgejo/`, etc.
Os subpacotes são detalhe interno daquele adapter (`entity`, `mapper`, `jpa`, `client`,
`config`). Não existe subpacote `adapter/` dentro de `adapter/` — o nome do pacote pai
já diz que aquilo é um adaptador. `mapper/` só existe quando a conversão é não-trivial;
mapeamento de um campo pode ficar inline no adapter.

**Nomes de porta descrevem a capacidade, não a tecnologia.** `CodeHostingPort`, não
`GitPort`/`ForgejoPort` — trocar de forge não deve renomear a porta. O sufixo distingue
a direção: outbound termina em `...Port`, inbound em `...UseCase`.

**A regra vive no tipo, a orquestração no use case.** Se uma regra pode ser violada
por um caller distraído, ela pertence ao domínio — `Slug` deriva e valida a si próprio,
`Project.create`/`Application.create` geram o id e o slug. O use case só busca, decide o
que fazer e salva. `SlugGenerator` como `object` estático não existe mais: era regra de
negócio fora de qualquer tipo, e a assinatura `existsBySlug(String)` aceitava tanto o slug
quanto o nome sem reclamar.

**Uma porta inbound por operação, nomeada como frase verbal.** Se descrever a interface
exige um "e", ela são duas (`GetBuildUseCase` + `ListBuildsUseCase`, não um `GetBuildUseCase`
com `get` e `list`). O custo é o número de arquivos; a válvula de escape, quando doer, é
manter porta só para comandos e deixar as queries irem do controller direto ao read service.
A assimetria é intencional: portas outbound se pagam porque a infra de fato troca e os
testes precisam de fake, enquanto uma porta inbound de leitura costuma ter um impl e um
caller para sempre.

**Nada específico de adapter atravessa o hexágono.** Um adapter outbound traduz sua
exceção de transporte (ex: `ForgejoException`) para a exceção declarada junto da porta
(`CodeHostingException`) antes de deixá-la subir. Sem isso, o adapter inbound precisaria
importar o adapter outbound para tratar o erro — acoplamento adapter→adapter.

Uma classe por arquivo, sem god classes.

## Fluxo de uma request

```
HTTP → XController (adapter.inbound.web.controller)
     → CreateXUseCase (application.port.inbound)
     → CreateXUseCaseImpl (application.usecase) — valida, orquestra
     → XRepositoryPort (application.port.outbound)
     → XPersistenceAdapter (adapter.outbound.persistence)
     → Postgres
```

## Vantagens

- Domínio testável sem banco/HTTP (mock nas portas).
- Troca de infra sem tocar no núcleo (Postgres → Mongo = novo adapter).
- Framework fica na borda.

## Custo

- Mais boilerplate (interface + impl + mapper).
- Over-engineering para CRUD simples.

## Rodando local

```bash
docker-compose up -d postgres forgejo   # compose v2 ausente em algumas máquinas — usar docker-compose (hífen)
export FORGEJO_TOKEN=<token de API gerado no Forgejo>
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

O token nunca vai versionado. Gere em `http://localhost:3000/user/settings/applications`
(escopo `write:repository`) e exporte no shell ou num `.env` local — `application-local.yml`
só declara `base-url`, o token vem de `${FORGEJO_TOKEN}`.

- Referência viva da API (Swagger UI, gerada do código): `http://localhost:8080/swagger-ui.html`
- Spec OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Testes que sobem Postgres via Testcontainers precisam de Docker rodando.

## Qualidade

```bash
./gradlew compileKotlin
./gradlew test
./gradlew detekt             # gate de lint/formatting do projeto
```

## Migrations

Flyway roda no boot; `ddl-auto=validate` só confere o schema criado pelas migrations.
Adicione arquivos em `src/main/resources/db/migration/` no padrão `V<n>__<descricao>.sql`.
