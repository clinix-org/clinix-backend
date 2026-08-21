# Clinix API (Backend)

API RESTful desenvolvida em **Java 17** e **Spring Boot 3** para a gestão hospitalar e clínica do sistema **Clinix**, provendo autenticação segura, persistência de dados e documentação interativa.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![Maven](https://img.shields.io/badge/Build-Maven-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue)
![License](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)

---

## Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Execução Rápida (H2 em Memória)](#-execução-rápida-h2-em-memória)
- [Persistência com PostgreSQL via Docker (Opcional)](#-persistência-com-postgresql-via-docker-opcional)
- [Documentação da API (Swagger)](#-documentação-da-api-swagger)
- [Frontend (Interface Gráfica)](#️-frontend-interface-gráfica)
- [Encerrando o Ambiente](#-encerrando-o-ambiente)
- [Solução de Problemas (Troubleshooting)](#-solução-de-problemas-troubleshooting)

---

## Sobre o Projeto

O **Clinix API** é o *core* backend responsável por gerenciar regras de negócio, dados de pacientes, agendamentos e controle de acesso para a plataforma **Clinix**.

Projetado sob arquitetura desacoplada e padrões modernos de segurança, o sistema oferece endpoints para integração com clientes **web** e **mobile**, utilizando **Spring Security**, **JWT** e **Spring Data JPA**.

---

## Tecnologias

| Tecnologia | Finalidade |
| :--- | :--- |
| **Java 17** | Linguagem principal de desenvolvimento |
| **Spring Boot 3.5** | Framework base para construção da API REST |
| **Spring Data JPA / Hibernate** | Mapeamento objeto-relacional e persistência |
| **H2 Database** | Banco de dados em memória para execução rápida |
| **PostgreSQL 18** | Banco de dados relacional principal (persistente) |
| **Spring Security & JWT** | Autenticação stateless e controle de autorização |
| **Springdoc OpenAPI (Swagger)** | Documentação interativa dos endpoints |
| **Maven** | Gerenciamento de dependências e build |
| **Docker & Docker Compose** | Conteinerização do banco de dados |

---

## Pré-requisitos

Antes de iniciar, certifique-se de ter instalado em sua máquina:

```text
✔ JDK 17 ou superior
✔ Apache Maven 3.8+ (opcional — o wrapper ./mvnw já está incluso no projeto)
```

Verifique sua versão do Java com:

```bash
java -version
```

> Você **não precisa** instalar o Maven separadamente. O projeto já inclui o *wrapper* (`mvnw` / `mvnw.cmd`), que baixa a versão correta automaticamente.

---

## Execução Rápida (H2 em Memória)

Esta é a forma **mais rápida** de subir o backend, sem necessidade de instalar bancos de dados ou Docker. Ideal para testes e primeiro contato com o projeto.

### Passo 1 — Clonar o repositório

```bash
git clone https://github.com/seu-usuario/clinix-backend.git
cd clinix-backend
```

### Passo 2 — Configurar variáveis de ambiente

Crie o arquivo de variáveis locais a partir do modelo:

```bash
cp .env.example .env.local
```

> Por padrão, o `.env.local` já vem configurado para usar o **H2 em memória** — você não precisa alterar nada nesta etapa para a execução rápida.

### Passo 3 — Executar a API

Escolha o bloco de comandos correspondente ao seu sistema operacional.

#### Linux / macOS

Na primeira execução, dê permissão ao script `mvnw`:

```bash
chmod +x mvnw
```

Execute o backend carregando as variáveis do `.env.local`:

```bash
(set -a; source .env.local; set +a; ./mvnw clean spring-boot:run)
```

Se o comando acima falhar, use esta alternativa:

```bash
export $(grep -v '^#' .env.local | xargs) && ./mvnw spring-boot:run
```

#### Windows (PowerShell)

```powershell
Get-Content .env.local | ForEach-Object {
  if ($_ -match '^([^=]+)=(.*)$') {
    [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2])
  }
}
.\mvnw spring-boot:run
```

### Pronto!

Se tudo correu bem, a API estará no ar em:

```text
http://localhost:8080
```

---

## Persistência com PostgreSQL via Docker (Opcional)

Por padrão os dados gravados no **H2** são perdidos ao desligar a aplicação. Se você deseja manter a persistência dos dados, utilize o **PostgreSQL via Docker**.

### Passo 1 — Verificar/instalar o Docker

Verifique se o Docker já está instalado e em execução:

```bash
docker --version
```

Caso ainda não tenha o Docker instalado, siga a documentação oficial do seu sistema operacional:

```text
Windows (Docker Desktop):
https://docs.docker.com/desktop/setup/install/windows-install/

Linux (Docker Engine):
https://docs.docker.com/engine/install/
```

> **Atenção usuários Windows:** o Docker Desktop exige virtualização ativa na BIOS e o **WSL2** instalado. Veja o passo a passo completo na seção [Solução de Problemas](#-solução-de-problemas-troubleshooting) antes de continuar.

### Passo 2 — Ajustar o arquivo `.env.local`

Abra o `.env.local`, **comente** o bloco do H2 e **descomente** o bloco do PostgreSQL:

```env
# --- Opção 1: H2 Banco em Memória (COMENTAR ESTE BLOCO)
# DB_URL=jdbc:h2:mem:clinix
# DB_USERNAME=sa
# DB_PASSWORD=
# DB_DDL_AUTO=create-drop

# --- Opção 2: PostgreSQL via Docker (DESCOMENTAR ESTE BLOCO)
DB_URL=jdbc:postgresql://localhost:5432/clinix
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_DDL_AUTO=update
```

### Passo 3 — Subir o container do PostgreSQL

> Antes de executar o comando abaixo, **abra o aplicativo Docker Desktop** (no Windows) e aguarde o status ficar como **"Running" / iniciado**.

Na raiz do projeto, execute:

```bash
docker compose up -d
```

Confirme se o container subiu corretamente:

```bash
docker compose ps
```

A saída esperada deve conter algo semelhante a:

```text
NAME       IMAGE          STATUS
postgres   postgres:18    Up
```

### Passo 4 — Conferir o banco de dados (opcional)

Conecte-se ao container para validar a conexão:

```bash
docker exec -it postgres psql -U postgres
```

Dentro do console do `psql`, liste os bancos criados:

```sql
\l
```

Para sair do console:

```sql
\q
```

### Passo 5 — Iniciar a API conectada ao PostgreSQL

Com o `.env.local` já ajustado (Passo 2), execute novamente o comando do Maven Wrapper referente ao seu sistema operacional, exatamente como no [Passo 3 da execução rápida](#passo-3--executar-a-api).

A API agora se conectará automaticamente ao container do PostgreSQL, e os dados persistirão entre reinicializações.

---

## Documentação da API (Swagger)

Após a inicialização da aplicação (seja em modo H2 ou PostgreSQL), a documentação interativa dos endpoints fica disponível automaticamente:

```text
Swagger UI:      http://localhost:8080/swagger-ui.html
OpenAPI Specs:   http://localhost:8080/v3/api-docs
```

---

## Frontend (Interface Gráfica)

O backend não possui interface visual própria. Para navegar pelo sistema **Clinix** através de uma interface web, clone e execute o frontend seguindo as instruções do próprio repositório:

```bash
git clone https://github.com/clinix-org/clinix-frontend.git
```

> Leia o `README.md` do repositório do frontend para configurá-lo corretamente e conectá-lo a esta API.

---

## Encerrando o Ambiente

Para interromper o container do PostgreSQL **mantendo a persistência dos dados já gravados**:

```bash
docker compose down
```

> Este comando **não apaga** os dados — eles ficam salvos no volume Docker. Para removê-los definitivamente, use `docker compose down -v` (use com cautela).

---

## Solução de Problemas (Troubleshooting)

### Windows: erro ao instalar/rodar o Docker Desktop

O Docker Desktop no Windows depende da **virtualização** estar habilitada na BIOS e do **WSL2** instalado.

**1. Verifique se a virtualização está ativa:**

Abra o Gerenciador de Tarefas → aba **Desempenho** → **CPU** e confira se "Virtualização" está como *Ativada*. Se estiver desativada, será necessário habilitá-la manualmente na BIOS/UEFI da placa-mãe.

**2. Verifique se o WSL2 está instalado:**

```powershell
wsl --status
```

**3. Caso não esteja instalado, instale com:**

```powershell
wsl --install
```

**4. Reinicie o computador** antes de tentar abrir o Docker Desktop novamente.

---

### O comando `./mvnw` falha com "Permission denied" (Linux/macOS)

Dê permissão de execução ao script:

```bash
chmod +x mvnw
```

---

### As variáveis do `.env.local` não estão sendo carregadas (Linux/macOS)

Use o comando alternativo de exportação:

```bash
export $(grep -v '^#' .env.local | xargs) && ./mvnw spring-boot:run
```

---

### O container do PostgreSQL não sobe ou a porta 5432 já está em uso

Verifique se já existe outro serviço PostgreSQL rodando na máquina e finalize-o, ou altere a porta exposta no `docker-compose.yml` e atualize `DB_URL` no `.env.local` de acordo.

---

### A API sobe, mas não conecta ao PostgreSQL

Confirme que:

```text
1. O container está com status "Up" → docker compose ps
2. As credenciais em .env.local batem com as do docker-compose.yml
3. O bloco do H2 está comentado e o do PostgreSQL está descomentado
```