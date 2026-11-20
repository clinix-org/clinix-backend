# Clinix Backend — orientações para agentes e colaboradores

## Visão geral do projeto

API REST da plataforma Clinix, responsável pelas regras de negócio, autenticação,
controle de acesso, usuários, medicamentos e auditoria. O frontend fica no
repositório separado `clinix-org/clinix-frontend`.

- Stack: Java 17, Spring Boot 3.5.5, Spring MVC, Spring Data JPA, Bean Validation,
  Spring Security com JWT, Maven Wrapper e documentação OpenAPI/Springdoc.
- PostgreSQL em ambientes persistentes; H2 em memória para início rápido e testes.
- Fontes de referência: `pom.xml`, `README.md`, `docs/medications.md`,
  `src/main/resources/application*.properties`, `Dockerfile` e `docker-compose.yml`.
- Antes de alterar instruções, confirme versões e comportamentos nesses arquivos.

## Fluxo de trabalho

- Leia os arquivos envolvidos e execute `git status --short` antes de editar.
  Preserve alterações de outras tarefas, inclusive arquivos ainda não versionados.
- Faça alterações focadas no pedido. Siga os padrões dos arquivos próximos e evite
  refatorações, mudanças de dependências ou reformatações sem relação com a tarefa.
- Atualize a documentação e os testes afetados por mudanças de comportamento.
- Ao concluir, informe o que mudou, quais verificações executou e eventuais
  limitações. Não declare testes aprovados se não foram executados.
- Não faça commit, push ou implantação em produção sem pedido do usuário.

## Estrutura e estilo de código

O pacote base é `com.gestao.clinix`, em `src/main/java/com/gestao/clinix/`.

| Diretório | Responsabilidade |
| --- | --- |
| `controller/` | Rotas HTTP, validação de entrada e códigos de resposta |
| `service/` | Regras de negócio, transações, permissões e auditoria |
| `repository/` | Persistência e consultas Spring Data JPA |
| `entity/` | Mapeamento JPA, relacionamentos e restrições de persistência |
| `dto/` e `mapper/` | Contratos de entrada/saída e conversão de dados |
| `config/` e `security/` | JWT, CORS, contexto autenticado e configuração HTTP |
| `exception/` | Exceções e tratamento centralizado de erros |
| `utils/` | Utilitários existentes, incluindo bootstrap de usuários |

- Preserve a arquitetura simples de controller, service, repository e DTO; use
  injeção por construtor e dependências `final` quando aplicável.
- Use classes em `PascalCase`, métodos e campos em `camelCase`, constantes em
  `UPPER_SNAKE_CASE`. Preserve a indentação do arquivo editado.
- Use APIs compatíveis com Java 17 e imports `jakarta.*` para JPA e validação.
- Valide requests com Bean Validation e `@Valid`; implemente regras de negócio no
  service. Use DTOs para controlar os campos aceitos e retornados pela API.
- Reutilize `GlobalExceptionHandler`, `ApiErrorResponse`, autenticação e
  `SystemLogService`; preserve os códigos HTTP e a correlação por `requestId`.
- Delimite operações atômicas com `@Transactional` no service e considere
  `readOnly = true` em consultas. Não dependa de sessão JPA aberta no controller:
  `spring.jpa.open-in-view` está desabilitado.
- Contratos novos de medicamentos usam inglês. A interface e mensagens ao usuário
  usam português. Não renomeie contratos legados apenas por padronização.

## Configuração e execução local

1. Instale JDK 17 e confirme `java -version`. Prefira o Maven Wrapper do projeto;
   ele baixa a distribuição Maven configurada em `.mvn/wrapper/` no primeiro uso.
2. Copie `.env.example` para `.env.local`, caso o arquivo local ainda não exista.
   Não sobrescreva configurações pessoais. Use apenas um bloco de banco por vez.
3. Carregue as variáveis na sessão antes de executar pelo Maven, conforme o
   `README.md`. O Maven não importa `.env.local` automaticamente; o Compose o
   carrega por `env_file`.
4. Para o primeiro contato, use H2 com profile `local`. Seus dados são temporários
   e o exemplo usa `DB_DDL_AUTO=create-drop`.

Variáveis principais: `SPRING_PROFILES_ACTIVE`, `PORT`, `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD`, `DB_DDL_AUTO`, `JWT_SECRET`, `JWT_EXPIRATION_MS`,
`CORS_ALLOWED_ORIGINS`, `JPA_SHOW_SQL` e `SWAGGER_ENABLED`.
O bootstrap usa `BOOTSTRAP_ADMIN_ENABLED` e `ADMIN_EMAIL`, `ADMIN_PASSWORD`,
`ADMIN_NAME`, `ADMIN_ACTIVE`; habilite-o apenas quando necessário.

| Profile | Uso e comportamento atual |
| --- | --- |
| `local` | Padrão; CORS para localhost, Swagger habilitado e DDL configurável |
| `dev` | Desenvolvimento compartilhado; exige origens CORS explícitas |
| `prod` | DDL `validate` por padrão, SQL sem exibição e Swagger desabilitado por padrão |
| `test` | H2 isolado, `create-drop`, bootstrap e Swagger desabilitados |

## Comandos de construção e teste

Execute os comandos na raiz. A execução da aplicação exige as variáveis de
ambiente; a suíte usa as configurações próprias de teste.

| Ação | Windows (PowerShell) | Linux/macOS |
| --- | --- | --- |
| Executar testes | `.\mvnw.cmd test` | `./mvnw test` |
| Build completo com testes | `.\mvnw.cmd clean verify` | `./mvnw clean verify` |
| Gerar JAR com testes | `.\mvnw.cmd clean package` | `./mvnw clean package` |
| Iniciar API | `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| Testar medicamentos | `.\mvnw.cmd -Dtest=MedicationIntegrationTest test` | `./mvnw -Dtest=MedicationIntegrationTest test` |

Em Linux/macOS, conceda permissão com `chmod +x mvnw` se necessário.
O JAR atual é `target/clinix-0.0.1-SNAPSHOT.jar`; confira o `pom.xml` se mudar a versão.
A API usa `http://localhost:8080` por padrão. Swagger: `/swagger-ui.html`;
OpenAPI: `/v3/api-docs`; saúde: `/actuator/health`.

Docker, com Docker Desktop/Engine em execução e `.env.local` configurado:

```sh
docker compose up -d --build api
docker compose logs -f api
docker compose ps
docker compose stop api
```

Para PostgreSQL local opcional, execute `docker compose up -d postgres` e confira
`docker compose exec postgres pg_isready -U postgres -d clinix_db` antes de iniciar
a API. Use `localhost:5432` na URL JDBC para API na máquina e `postgres:5432`
para API no Compose. O Compose fixa a API na porta 8080.
`docker compose down` preserva o volume; não use `down -v` para parar o ambiente.

## Instruções de teste

- Execute `.\mvnw.cmd test` ou `./mvnw test` antes de concluir uma alteração.
  Registre falhas de ambiente separadamente de falhas da implementação.
- Os testes ficam em `src/test/java`; os de contexto/integração usam
  `@ActiveProfiles("test")` e `src/test/resources/application-test.properties`.
  Não os aponte para bancos compartilhados ou de produção.
- Use JUnit 5, AssertJ e Mockito conforme os testes existentes. Testes unitários
  cobrem regras dos services; testes HTTP usam Spring Boot e MockMvc.
- Para mudanças funcionais, cubra o caso esperado, entradas inválidas, limites e
  regressões relevantes. Testes da API devem cobrir autenticação, autorização,
  validação, auditoria e concorrência quando aplicáveis à operação.
- Em medicamentos, preserve a cobertura de composição, normalização, duplicidade
  simultânea, estoque inicial zero e preservação da criação na edição.
- H2 em modo PostgreSQL não comprova compatibilidade de migrações: valide SQL e
  constraints também em PostgreSQL numa branch de banco isolada.
- Relatórios Maven ficam em `target/surefire-reports/`. O build do Docker usa
  `-DskipTests`; execute a suíte separadamente antes de gerar a imagem para entrega.

## Regras de medicamentos e contratos

- Consulte `docs/medications.md` antes de alterar `/medications`. Coordene DTOs,
  nomes de campos, paginação, opções e respostas com o frontend.
- Toda escrita de medicamentos exige ADMIN no backend. Preserve a proteção de
  POST, PUT e DELETE; a leitura exige autenticação.
- Obtenha o responsável da sessão autenticada, usando os serviços existentes;
  nunca aceite auditoria ou estoque inicial no payload de cadastro.
- Salve cadastro, composição e auditoria na mesma transação. O cadastro começa
  com `quantity: 0` e `status: UNAVAILABLE`; preserve estoque, `createdAt` e
  `createdBy` na edição. Use `BigDecimal` para dosagens.
- Duplicidade usa nome comercial normalizado e composição completa (nome,
  dosagem e unidade), sem considerar ordem, fabricante ou apresentação.
  Valores 500 e 500.0 são equivalentes; não há conversão automática entre unidades.
- Preserve a constraint `uk_medications_duplicate_key` para concorrência e a
  resposta 409 com “Medicamento já cadastrado.”. Uma consulta prévia não substitui
  a unicidade no banco.
- As listas de seleção vivem em `MedicationOptions` e são expostas pela API.
  `mgc` é o código solicitado para micrograma; não o altere sem coordenar o contrato.
- Preserve os identificadores de permissão `medicamentos:*` e a navegação do
  frontend em `/medicamentos`, mesmo com a API em inglês.

## Considerações de segurança

- Não versione credenciais, tokens, chaves, dumps ou `.env.local`. Exemplos devem
  conter apenas valores fictícios; não reutilize as credenciais de demonstração
  em ambientes compartilhados ou produção.
- Não exponha senhas, JWTs, headers de autorização ou dados sensíveis nos logs,
  respostas, documentação ou relatórios de execução.
- Preserve Spring Security stateless, validação JWT e BCrypt para senhas.
  Reutilize `AuthenticatedUserProvider`, `UserContextService` e
  `AccessPolicyService` conforme a responsabilidade; nunca confie em permissões
  ou responsáveis enviados pelo cliente.
- `/users/**` exige ADMIN. Não amplie rotas públicas nem remova controles de
  autorização para contornar falhas de teste ou integração.
- `JWT_SECRET` precisa ter pelo menos 32 bytes e `JWT_EXPIRATION_MS` ser positivo.
  Use segredo próprio por ambiente, fornecido pela configuração segura do deploy.
- Configure CORS com as origens necessárias. Em produção, use profile `prod`,
  mantenha Swagger desabilitado salvo necessidade explícita e bootstrap desligado
  após o provisionamento. Não exponha stack traces nem detalhes internos de erros.

## Banco de dados e migrações

- Migrações PostgreSQL ficam em `docs/database/` e são aplicadas explicitamente
  antes da nova API; não há executor automático. Não introduza essa suposição
  nos comandos de build ou de inicialização.
- Teste primeiro numa branch de banco isolada, confira schema/dados e preserve
  constraints, relacionamentos, estoque e histórico de auditoria.
- Não use `ddl-auto=update` como substituto de uma migração de renomeação.
  Em produção, mantenha `DB_DDL_AUTO=validate`; `create` e `create-drop` são
  inadequados para bancos persistentes com dados a preservar.
- `V001__medications.sql` é de aplicação única sobre o schema legado: exige a
  tabela `medicamento` vazia e interrompe a execução se encontrar registros.
  Preserve esses dados e prepare mapeamento explícito; não apague registros para
  contornar a proteção nem reaplique o script num schema já migrado.
- Siga `docs/medications.md` para a conexão PostgreSQL direta e a execução com
  `psql` e `ON_ERROR_STOP=1`. A migração existente não é um provisionador completo
  de um banco PostgreSQL vazio.

## Etapas de implantação

Não há pipeline de deploy versionado em `.github/workflows/`. Use o destino
definido pela equipe e só execute implantação em produção quando solicitada.

1. Revise as alterações, contratos do frontend, configurações necessárias e
   compatibilidade do schema. Execute `clean verify` com o Maven Wrapper.
2. Valide migrações em PostgreSQL isolado. Antes de aplicar no destino autorizado,
   confirme banco/branch, backup ou ponto de restauração e plano de recuperação.
3. Configure as variáveis no ambiente de destino: profile `prod`, conexão do
   banco, credenciais, segredo JWT e origens CORS; mantenha DDL em `validate`.
   Para banco gerenciado, use a configuração TLS exigida pelo provedor.
4. Se houver migração incompatível, pare a API antiga, aplique explicitamente
   os scripts pendentes na ordem documentada e só então inicie a API nova.
5. Gere o JAR com `clean package` ou a imagem pelo `Dockerfile`. O container
   executa com usuário sem privilégios; preserve essa configuração. No Compose,
   use `docker compose up -d --build api` com as variáveis do destino.
6. Confira inicialização e `/actuator/health`. Valide login, acesso ADMIN/USER e
   os fluxos afetados, incluindo persistência e auditoria, sem alterar dados
   reais para testes sem autorização.
7. Monitore os logs sem expor segredos. Se houver falha, interrompa o avanço e
   execute o plano de recuperação compatível com o schema; restaurar apenas
   a imagem anterior pode não reverter uma migração de banco.
