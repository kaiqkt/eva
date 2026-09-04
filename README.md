# Eva Server

Spring Boot (Kotlin) seguindo **Arquitetura Hexagonal (Ports & Adapters)**.

## Conceitos

O núcleo de negócio fica isolado — não conhece banco, HTTP ou fila. O mundo externo só fala com o núcleo através de **portas** (interfaces), e **adaptadores** implementam essas portas.

- **Domínio / Core** — regras de negócio puras. Zero dependência de framework.
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
│   ├── model/                     # entidades e value objects de negócio
│   └── service/                   # regras de domínio que não pertencem a um único modelo
│
├── application/                   # orquestração dos casos de uso
│   ├── port/
│   │   ├── inbound/               # driving ports (interfaces) — Create/Find/Update/Delete...UseCase
│   │   └── outbound/              # driven ports (interfaces) — ...RepositoryPort, ...ClientPort
│   └── usecase/                   # implementa port.inbound, usa port.outbound — ...Service
│
└── adapter/                       # tudo que toca o mundo externo
    ├── inbound/
    │   └── web/
    │       ├── controller/        # controllers REST (finos: validam entrada, delegam ao use case)
    │       ├── request/           # DTOs de entrada (request bodies)
    │       ├── response/          # DTOs de saída — domain nunca vaza pra borda
    │       ├── advice/            # @RestControllerAdvice — traduz exceção de domínio em ProblemDetail
    │       ├── support/           # helpers de parsing compartilhados
    │       └── config/            # OpenApiConfig (springdoc)
    └── outbound/
        └── persistence/           # entity/mapper/repository JPA — implementa os RepositoryPort
```

**Convenção de nomes de pacote: sempre no singular** (`controller`, `request`, `response`,
`client`, `mapper`, `config`, `adapter`), seguindo o padrão Java/Kotlin.

**Padrão dos subpacotes de adapter:** dentro de cada sistema externo, separar por
responsabilidade — `adapter` (orquestra e implementa o port), `client` (transporte),
`config` (beans/properties), `mapper` (conversão pro domain). No inbound web:
`controller`, `request`, `response`, `advice`, `support`. Uma classe por arquivo, sem god classes.

## Fluxo de uma request

```
HTTP → XController (adapter.inbound.web.controller)
     → CreateXUseCase (application.port.inbound)
     → CreateXService (application.usecase) — valida, orquestra
     → XRepositoryPort (application.port.outbound)
     → XRepositoryAdapter (adapter.outbound.persistence)
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
