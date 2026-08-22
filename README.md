# Clinix API (Backend)

API RESTful em **Java 17 + Spring Boot 3** para o sistema **Clinix**.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![Maven](https://img.shields.io/badge/Build-Maven-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue)
![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)

---

## Sobre o projeto

O **Clinix API** é o backend responsável pelas regras de negócio e pelo controle de acesso da plataforma **Clinix**.

Acesse a plataforma [Clinix](https://clinix-saude.vercel.app/).

---

## Pré-requisitos

```text
✔ JDK 17 ou superior      → java -version

✔ Apache Maven 3.8+       → mvn --version
```
---

## Início rápido (H2 em memória)

Sem instalar banco de dados nem Docker. Ideal para o primeiro contato com o projeto.

**1. Clonar e entrar no projeto**

```bash
git clone https://github.com/clinix-org/clinix-backend.git
cd clinix-backend
```

**2. Criar o `.env.local`**

```bash
cp .env.example .env.local
```

O `.env.local` já vem configurado para H2 por padrão, nenhum ajuste é necessário aqui.

**3. Rodar o backend**

Linux / macOS (primeira vez, dar permissão ao script):

```bash
chmod +x mvnw
```

```bash
(set -a; source .env.local; set +a; ./mvnw clean spring-boot:run)
```

Se falhar, use:

```bash
export $(grep -v '^#' .env.local | xargs) && ./mvnw clean spring-boot:run
```

Windows (PowerShell):

```powershell
Get-Content .env.local | ForEach-Object {
  if ($_ -match '^([^=]+)=(.*)$') {
    [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2])
  }
}
.\mvnw clean spring-boot:run
```

**Pronto.** A API sobe em `http://localhost:8080`.

---

## PostgreSQL via Docker (opcional, com persistência)

Use esta seção se quiser que os dados **não se percam** ao desligar a aplicação.

> **Não pule esta parte.** A causa mais comum de erro na instalação é tentar subir o container do Postgres sem esses pré-requisitos ativos, especialmente no Windows.

### Checklist antes de continuar

Confirme cada item, na ordem, **antes** de rodar qualquer `docker compose`:

| # | Verificação | Como checar | O que fazer se falhar |
|---|---|---|---|
| 1 | Docker instalado | `docker --version` | Instalar: [docs.docker.com/engine/install](https://docs.docker.com/engine/install/) (Linux) ou [Docker Desktop](https://docs.docker.com/desktop/setup/install/windows-install/) (Windows) |
| 2 | *(Windows)* Virtualização ativa na BIOS | Gerenciador de Tarefas → Desempenho → CPU → "Virtualização" | Ativar manualmente na BIOS/UEFI da placa-mãe |
| 3 | *(Windows)* WSL2 instalado | `wsl --status` | `wsl --install` e **reiniciar o computador** |
| 4 | Docker Desktop está rodando | Abrir o app e aguardar status "Running" | Abrir o Docker Desktop manualmente e esperar |
| 5 | Docker responde a comandos | `docker ps` (não pode dar erro de conexão) | Se der erro, repita os itens 2–4 e reinicie o computador |

Só avance depois que **todos os 5 itens acima passarem sem erro.**

### Passo 1 — Ajustar o `.env.local`

Comente o bloco do H2 e descomente o bloco do PostgreSQL:

```env
# --- Opção 1: H2 (COMENTAR)
# DB_URL=jdbc:h2:mem:clinix_db
# DB_USERNAME=sa
# DB_PASSWORD=
# DB_DDL_AUTO=create-drop

# --- Opção 2: PostgreSQL via Docker (DESCOMENTAR)
DB_URL=jdbc:postgresql://localhost:5432/clinix_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_DDL_AUTO=update
```

--- 

```markdown
> **Nota sobre os comandos Docker**
>
> Os próximos comandos Docker funcionam normalmente no **PowerShell do Windows**.
>
> No **Linux**, adicione `sudo` antes dos comandos quando necessário. Por exemplo:
>
> **Windows (PowerShell):**
>
> ```bash
> docker compose up -d
> ```
>
> **Linux:**
>
> ```bash
> sudo docker compose up -d
> ```

---

### Passo 2 — Subir o container

```bash
docker compose up -d
```

Isso cria automaticamente o banco de dados **`clinix_db`** dentro do container, você não precisa criar nada manualmente.

Confirme se subiu:

```bash
docker compose ps
```

Saída esperada:

```text
NAME              IMAGE          STATUS
clinix-postgres   postgres:18    Up
```

### Passo 3 — (Opcional) Verificar se o banco `clinix_db` foi criado

```bash
docker exec -it clinix-postgres psql -U postgres
```

Dentro do console `psql`:

```sql
\l
```

Você deve ver `clinix_db` na lista de bancos. Para sair:

```sql
\q
```

### Passo 4 — Rodar a API

Repita o comando do [Início rápido, passo 3](#início-rápido-h2-em-memória) referente ao seu sistema operacional. A API vai se conectar automaticamente ao Postgres.

---

### Depois de reiniciar o computador

O container **não sobe sozinho** ao ligar a máquina. Antes de rodar o backend novamente, suba o container manualmente:

```bash
docker start clinix-postgres
```

Só depois disso, execute o backend normalmente.

---

## Documentação da API (Swagger)

```text
Swagger UI:     http://localhost:8080/swagger-ui.html
OpenAPI Specs:  http://localhost:8080/v3/api-docs
```

---

## Frontend

O **Clinix API** é o backend da aplicação e não possui uma interface gráfica própria. A interface web do Clinix está disponível em um repositório separado.

Para executar o Clinix localmente com a **interface web acessível pelo navegador**, é necessário clonar, configurar e executar também o repositório do frontend:

```bash
git clone https://github.com/clinix-org/clinix-frontend.git
```

---

## Encerrar o ambiente

Para parar o Postgres **mantendo os dados salvos**:

```bash
docker compose down
```

---

## Referência rápida de diagnóstico

| Sintoma | Comando de checagem |
| :--- | :--- |
| Docker não responde | `docker ps` |
| Container existe mas está parado | `docker compose ps` → depois `docker start clinix-postgres` |
| WSL2 instalado? | `wsl --status` |
| Permissão negada no `./mvnw` | `chmod +x mvnw` |
| Variáveis do `.env.local` não carregam | `export $(grep -v '^#' .env.local \| xargs) && ./mvnw clean spring-boot:run` |
| Porta 5432 já em uso | Pare o serviço Postgres local ou altere a porta no `docker-compose.yml` e no `.env.local` |

---

## Suporte e Ajuda

Encontrou um problema, identificou um bug ou tem alguma dúvida relacionada ao **Clinix API**?

Utilize o sistema de **Issues do GitHub** para registrar sua solicitação e contribuir para o acompanhamento e resolução das questões do projeto.

- **🐛 Bug:** [Relatar um problema](https://github.com/clinix-org/clinix-backend/issues/new)
- **💡 Dúvida ou sugestão:** [Abrir uma Issue](https://github.com/clinix-org/clinix-backend/issues)
- **📋 Issues existentes:** [Consultar Issues](https://github.com/clinix-org/clinix-backend/issues)
- **🌐 Plataforma Clinix:** [Acessar a plataforma](https://clinix-saude.vercel.app/)

> Ao abrir uma Issue, selecione o tipo de solicitação adequado e forneça o máximo de informações possível.
