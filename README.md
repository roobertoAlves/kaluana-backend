# Kaluanã - API (back-end)

API REST do Kaluanã, aplicativo de apoio à organização e à autorregulação de
pessoas com TDAH. O aplicativo móvel (repositório `kaluana-frontend`) consome
esta API.

Tecnologias: Java 21, Spring Boot 3.3, Spring Security com JWT, Spring Data
JPA, PostgreSQL 16, Flyway (migrações do banco), Redis 7 (limite de
requisições) e Docker Compose.

## 1. Pré-requisitos

Para rodar com Docker (recomendado):

- Docker Desktop (Windows/macOS) ou Docker Engine com o plugin Compose (Linux).

Para rodar sem Docker, apenas a API na máquina:

- JDK 21 e Maven 3.9 ou superior;
- PostgreSQL 16 e Redis 7 em execução.

## 2. Configuração

Copie o modelo de variáveis de ambiente e preencha os valores:

```bash
cp .env.example .env
```

| Variável | Para que serve |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Perfil: `dev`, `homolog` ou `prod` |
| `DB_URL` | Endereço do banco. Com Docker: `jdbc:postgresql://db:5432/kaluana` |
| `DB_USER`, `DB_PASSWORD` | Usuário e senha do PostgreSQL (o contêiner do banco é criado com eles) |
| `REDIS_HOST`, `REDIS_PORT` | Redis. Com Docker: `redis` e `6379` |
| `JWT_SECRET` | Chave de assinatura dos tokens, com pelo menos 32 caracteres. Sem ela a API não sobe |
| `FRONTEND_ORIGIN` | Origens aceitas pelo CORS, separadas por vírgula (ex.: `http://localhost:8081`) |

Outras configurações têm valor padrão e são opcionais: duração do token de
acesso (`JWT_EXPIRATION_MINUTES`, 30), validade do token de renovação
(`JWT_REFRESH_DAYS`, 30), validade do código de emparelhamento
(`PAIRING_CODE_EXPIRATION_MINUTES`, 15) e limite de tentativas de login
(`RATE_LIMIT_MAX_ATTEMPTS`, `RATE_LIMIT_WINDOW_SECONDS`). Os valores de cada
perfil estão em `src/main/resources/application-*.yml`.

O arquivo `.env` contém senhas e não deve ser enviado ao Git.

## 3. Como rodar com Docker

```bash
docker compose up -d --build
```

Isso sobe três contêineres: PostgreSQL, Redis e a API. Na primeira subida o
Flyway cria todas as tabelas automaticamente. A API fica em
`http://localhost:8080`.

Para conferir se está no ar:

```bash
curl http://localhost:8080/actuator/health
```

A resposta esperada é `{"status":"UP"}`. Outros comandos úteis:

```bash
docker compose logs -f backend
```

```bash
docker compose down
```

O `down` para os contêineres e mantém os dados. Para apagar também o banco,
use `docker compose down -v`.

### Produção (HTTPS)

`docker-compose.prod.yml` sobe a mesma API sem expor a porta 8080: quem
atende a internet é o Caddy (`Caddyfile`), que obtém o certificado HTTPS
automaticamente para o domínio em `DOMAIN`. Nesse arquivo o Redis exige
senha (`REDIS_PASSWORD`). Use um `.env` próprio de produção:

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

## 4. Como rodar sem Docker

Com PostgreSQL e Redis rodando na máquina, exporte as variáveis da seção 2
(usando `localhost` no lugar de `db` e `redis`) e execute:

```bash
mvn spring-boot:run
```

## 5. Testes

Testes automatizados (JUnit 5 e Mockito):

```bash
mvn test
```

Sem Java instalado, os mesmos testes rodam dentro de um contêiner Maven:

```bash
docker run --rm -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-21 mvn test
```

Testes de ponta a ponta da API: a pasta `postman/` tem a coleção
`kaluana_collection.json` e ambientes de exemplo (`env_dev.json`,
`env_homolog.json`, `env_prod.json`). Com a API no ar e o banco vazio, ela pode
ser importada no Postman ou executada pelo Newman:

```bash
npx newman run postman/kaluana_collection.json -e postman/env_dev.json
```

A coleção cria os próprios usuários e dados, então deve ser executada com o
banco recém-criado (`docker compose down -v` e depois `up` de novo).

## 6. Estrutura do código

O código fica em `src/main/java/br/edu/ifsp/kaluana`, organizado por
funcionalidade. Cada pasta tem seu controlador (rotas), serviço (regras),
repositório (banco) e DTOs (formato de entrada e saída).

| Pasta | Rotas | Conteúdo |
| --- | --- | --- |
| `auth` | `/api/auth` | Cadastro, login, renovação de sessão e saída |
| `pairing` | `/api/pairing` | Código de emparelhamento, vínculos e o que cada pessoa vinculada pode ver |
| `user` | `/api/users` | Perfil da conta |
| `event` | `/api/events` | Registro de eventos (RF1) |
| `reminder` | `/api/reminders` | Lembretes (RF2) |
| `checkin` | `/api/check-ins` | Check-in diário (RF3) |
| `contact` | `/api/contacts` | Contatos de confiança (RF4) |
| `task` | `/api/tasks` | Tarefas e microetapas (RF5) |
| `timer` | `/api/timer-sessions` | Histórico das sessões de foco (RF9) |
| `plan` | `/api/day-plan` | Plano do dia e replanejamento (RF10, RF14) |
| `assistant` | `/api/assistant` | Assistente de IA opcional, desligado por padrão (RF11) |
| `access` | `/api/access-settings` | Padrão de compartilhamento para novos vínculos (RF8) |
| `report` | `/api/reports` | Relatório informativo, inclusive para quem está vinculado (RF7) |
| `security`, `config`, `common` | - | JWT, CORS, limite de requisições e tratamento de erros |

As migrações do banco ficam em `src/main/resources/db/migration` e são
aplicadas em ordem (`V1`, `V2`, ...) sempre que a API sobe.

## 7. Autenticação

- `POST /api/auth/register/primary` e `POST /api/auth/login` devolvem um token
  de acesso (curta duração) e um token de renovação.
- As demais rotas exigem o cabeçalho `Authorization: Bearer <token de acesso>`.
- Quando o token de acesso vence, `POST /api/auth/refresh` troca o token de
  renovação por um par novo; `POST /api/auth/logout` encerra a sessão.
- Um responsável ou profissional cria a conta com o código gerado pelo
  usuário principal (`POST /api/auth/register/responsible`) e só vê o que
  esse usuário liberou para ele.

## 8. Assistente de IA (opcional)

O assistente vem desligado e a API funciona por completo sem ele. Para ligar,
defina `ASSISTANT_ENABLED=true` e aponte `ASSISTANT_BASE_URL` e
`ASSISTANT_MODEL` para um serviço compatível com a API de chat da OpenAI, por
exemplo um modelo local no Ollama (`http://localhost:11434/v1`). Com Docker,
essas variáveis precisam ser adicionadas ao serviço `backend` do
`docker-compose.yml`.

## 9. Problemas comuns

- A API não sobe e o log fala de `JWT_SECRET`: a chave está vazia ou tem
  menos de 32 caracteres.
- Porta 5432 ou 8080 em uso: pare o serviço que ocupa a porta ou altere o
  mapeamento em `docker-compose.yml`.
- O aplicativo recebe erro de CORS: inclua a origem dele em
  `FRONTEND_ORIGIN`.
- O aplicativo no celular não conecta: use o IP do computador na rede (não
  `localhost`) e libere a porta 8080 no firewall.
