# Cadastro de medicamentos

O cadastro está integrado ao frontend e restrito a administradores. O botão abre
um modal baseado no Figma 1628:4882. Todos os campos são obrigatórios. É possível
adicionar e remover princípios ativos, mantendo de 1 a 30 itens distintos.

## Contrato HTTP

Todas as rotas exigem autenticação. POST, PUT e DELETE exigem ADMIN.

| Método | Rota | Comportamento |
| --- | --- | --- |
| GET | `/medications` | Lista paginada com `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `lastPage` |
| GET | `/medications/options` | Unidades, categorias e apresentações com `value` e `label` |
| GET | `/medications/{id}` | Consulta individual |
| POST | `/medications` | Criação, status 201, corpo com o cadastro e header Location |
| PUT | `/medications/{id}` | Edição dos dados cadastrais, preservando estoque e criação |
| DELETE | `/medications/{id}` | Exclusão, status 204 |

```json
{
  "medicationName": "Medicamento exemplo",
  "activeIngredient": [
    { "name": "Princípio A", "dosage": 500, "unitMeasurement": "mg" }
  ],
  "category": "ANALGESIC",
  "presentationFormat": "TABLET",
  "manufacturer": "Fabricante exemplo"
}
```

Dosagem é positiva, com até 12 dígitos inteiros e 6 decimais. Nomes e fabricante
aceitam até 200 caracteres. A unidade corresponde ao valor informado; não há
conversão entre unidades. Concentrações como `mg/mL` usam o mesmo campo numérico.
A lista de unidades mantém `mgc` para micrograma conforme solicitado.

Duplicidade considera nome comercial e todos os princípios ativos, cada um com
nome, dosagem e unidade. Ignora caixa, espaços extras e ordem. Não considera
fabricante ou apresentação. Valores 500 e 500.0 são iguais; 1 g e 1000 mg são
distintos nesta versão. Princípios ativos repetidos no mesmo cadastro são inválidos.
Um conflito retorna 409 e “Medicamento já cadastrado.”, inclusive sob concorrência.

`createdAt` é um instante UTC e `createdBy` vem do usuário autenticado. O cadastro
começa com `quantity: 0` e `status: UNAVAILABLE`. Não há movimentação de estoque
nesse formulário. Cadastro e log de auditoria são atômicos.

A listagem aceita `search`, `pageNumber` (base zero), `pageSize` (1 a 100),
`sortBy` e `sortOrder` (`asc` ou `desc`). Campos de ordenação permitidos:
`medicationName`, `id`, `category`, `quantity`, `status`, `createdAt`.
O frontend mostra os mais recentes primeiro; após cadastro, limpa a busca e
retorna à primeira página. Busca e paginação continuam disponíveis.

## Migração e compatibilidade

O contrato antigo `/medicamentos` e campos em português foi substituído por
`/medications`. A listagem, os tipos e o consumo do frontend foram atualizados
na mesma entrega. A rota de navegação da interface permanece `/medicamentos`,
assim como os identificadores de permissão existentes `medicamentos:*`.

Não existe executor automático de migrações neste projeto. Pare a API antiga,
aplique uma vez `docs/database/V001__medications.sql` com conexão PostgreSQL direta
e só então inicie a API nova. Exemplo com uma variável de conexão já configurada:

```powershell
psql "$env:DATABASE_URL_UNPOOLED" -v ON_ERROR_STOP=1 -f docs/database/V001__medications.sql
```

A migração é transacional, renomeia a tabela e cria a coleção de princípios ativos.
Ela recusa execução se houver registros legados, pois não existem informações
suficientes para inferir composição, fabricante e auditoria. Nesse caso, preserve
os registros e prepare um mapeamento explícito; não apague dados para contornar
a verificação. Na branch `db-euler-dev`, a tabela estava vazia na análise inicial.

Em ambientes novos com H2, Hibernate cria o schema conforme o profile existente.
Em PostgreSQL já existente, execute a migração antes de usar `ddl-auto=validate`.
Não aplique essa migração em outras branches sem conferir seu schema e dados.

## Validação

Backend: `./mvnw test` ou `.\mvnw.cmd test`.
Frontend: `npm run lint`, `npm run build:development`, `npm run build:production`.
Os testes integrados cobrem cadastro, leitura, edição, exclusão, auditoria,
autenticação, permissão, validação, normalização e duas criações simultâneas.

Em 17/11/2026, a migração foi testada em uma branch filha isolada e aplicada na
`db-euler-dev`. A API local foi reconstruída e validada contra esse PostgreSQL:
criação, listagem, duplicidade, edição e exclusão. O registro temporário foi
removido pela API; seus eventos de auditoria permanecem como histórico do teste.
