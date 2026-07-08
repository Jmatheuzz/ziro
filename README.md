# Ziro

Sistema de gestão para microempreendedores — vendas, estoque, financeiro e clientes.

## Stack

- **Frontend:** React 18 + TypeScript + Vite + Tailwind CSS
- **Backend:** Spring Boot 3.3 + Java 21 + PostgreSQL
- **Infra:** Docker Compose

---

## Rodando com Docker (recomendado)

Sobe tudo de uma vez: banco, backend e frontend.

### Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando

### 1. Clone o repositório

```bash
git clone https://github.com/Jmatheuzz/ziro.git
cd ziro
```

### 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
```

Edite o `.env` e preencha os campos obrigatórios:

| Variável | Descrição |
|---|---|
| `DB_PASSWORD` | Senha do banco PostgreSQL |
| `JWT_SECRET` | Chave secreta JWT — use pelo menos 64 caracteres aleatórios |
| `MAIL_USERNAME` | Email do Gmail usado para enviar verificações de conta |
| `MAIL_PASSWORD` | App Password do Gmail (não é a senha da conta — gere em [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)) |

> As outras variáveis já têm valores padrão que funcionam em desenvolvimento.

### 3. Suba os containers

```bash
docker compose up --build -d
```

### 4. Acesse

| Serviço | URL |
|---|---|
| Frontend | http://localhost |
| Backend (API) | http://localhost:8080 |
| Adminer (banco) | http://localhost:8081 |

No Adminer, use: sistema `PostgreSQL`, servidor `postgres`, usuário/senha/banco conforme o `.env`.

### Parar os containers

```bash
docker compose down
```

Para remover também o volume do banco (apaga todos os dados):

```bash
docker compose down -v
```

---

## Rodando em modo desenvolvimento (sem Docker)

Use quando quiser hot-reload no frontend ou debugar o backend pela IDE.

### Pré-requisitos

- Java 21
- Node.js 20+
- PostgreSQL 16 rodando localmente (ou via Docker — veja abaixo)

### Subir só o banco via Docker

```bash
docker compose up postgres -d
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

O backend sobe em `http://localhost:8080`. As variáveis de ambiente podem ser definidas no sistema ou em um arquivo `.env` na raiz do projeto — o `application.yml` usa os valores padrão quando não estão definidas.

### Frontend

```bash
cd frontend
cp .env.example .env.local   # só na primeira vez
npm install
npm run dev
```

O frontend sobe em `http://localhost:5173` com hot-reload.

---

## Deploy em produção (AWS EC2)

Use o script `deploy.sh` na raiz do projeto. Ele instala Docker na instância, clona o repositório e sobe os containers automaticamente.

```bash
export EC2_HOST=<ip-da-instancia>
export EC2_KEY=~/.ssh/ziro.pem
export REPO_URL=https://github.com/Jmatheuzz/ziro.git
./deploy.sh
```

Antes de rodar, atualize no `.env` local:

```
VITE_API_URL=http://<ip-da-instancia>:8080
FRONTEND_URL=http://<ip-da-instancia>
```

O script envia o `.env` local para o servidor automaticamente.

> Requisitos na instância: Security Group com portas **22**, **80** e **8080** abertas.

---

## Testes

```bash
cd backend
./mvnw test
```

---

## Variáveis de ambiente — referência completa

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_NAME` | `ziro` | Nome do banco |
| `DB_USER` | `ziro` | Usuário do banco |
| `DB_PASSWORD` | — | Senha do banco |
| `DB_PORT_EXPOSED` | `5432` | Porta exposta do PostgreSQL |
| `JWT_SECRET` | — | Chave JWT (obrigatória) |
| `JWT_ACCESS_EXP_MIN` | `15` | Expiração do access token (minutos) |
| `JWT_REFRESH_EXP_DIAS` | `7` | Expiração do refresh token (dias) |
| `FRONTEND_URL` | `http://localhost,http://localhost:5173` | Origens permitidas no CORS |
| `MAIL_HOST` | `smtp.gmail.com` | Host SMTP |
| `MAIL_PORT` | `587` | Porta SMTP |
| `MAIL_USERNAME` | — | Email remetente |
| `MAIL_PASSWORD` | — | App Password do email |
| `VITE_API_URL` | `http://localhost:8080` | URL da API consumida pelo frontend |
| `BACKEND_PORT_EXPOSED` | `8080` | Porta exposta do backend |
| `FRONTEND_PORT_EXPOSED` | `80` | Porta exposta do frontend |
| `ADMINER_PORT_EXPOSED` | `8081` | Porta exposta do Adminer |
| `LOG_LEVEL` | `INFO` | Nível de log do backend |
