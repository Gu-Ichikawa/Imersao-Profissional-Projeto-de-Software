# ZENTRY — Backend

API REST do ZENTRY em Java com Spring Boot (monólito modular).

## Pré-requisitos

- Java 21 ou superior
- MySQL 8 rodando em `localhost:3306`

## Configurar o banco

O banco `zentry` é criado automaticamente na primeira execução.
Informe o usuário e a senha do **seu** MySQL por variáveis de ambiente (assim a senha não vai para o Git):

| Variável | Padrão | Para quê |
|---|---|---|
| `DB_USER` | `root` | usuário do MySQL |
| `DB_PASSWORD` | vazio | senha do MySQL |
| `DB_URL` | `jdbc:mysql://localhost:3306/zentry...` | só se o MySQL estiver em outro endereço |

**Git Bash** (vale só para o terminal aberto):

```bash
export DB_PASSWORD=suasenha
```

**IDE (STS / IntelliJ):** configure `DB_PASSWORD` em *Run Configuration → Environment*.

## Rodar

```bash
cd backend
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Deve aparecer `Started ZentryBackendApplication` no console.

## Testes

```bash
./mvnw test
```

Os testes sobem a aplicação inteira, então precisam do MySQL ligado e do `DB_PASSWORD` definido.

## Estrutura

```
src/main/java/br/com/zentry/
├── compartilhado/
│   ├── config/   → configurações gerais (CORS)
│   └── erro/     → formato padrão de erro e tratador global
├── cardapio/     → categorias, pratos e ficha técnica
└── estoque/      → insumos e movimentações

Cada módulo segue as camadas: controller → service → repository, com model (entidades JPA) e dto.
```

- Um módulo **não** acessa o repository de outro: usa o service público dele.
- O `compartilhado` não tem regra de negócio e não depende dos módulos.

## Convenções (proposta do grupo — mudanças via PR neste README)

### Pacotes e camadas

- Cada módulo (`cardapio`, `estoque`) tem: `controller`, `service`, `repository`, `model`, `dto`.
- **controller**: só recebe a requisição, valida com `@Valid` e chama o service. Sem regra de negócio.
- **service**: regras de negócio e `@Transactional`.
- **repository**: interfaces `JpaRepository`.
- **model**: entidades JPA (`@Entity`).
- **dto**: o que entra e sai da API. O controller **nunca** recebe nem devolve a entidade direto.
- Um módulo **não** usa o repository de outro: chama o service público dele.
- O `compartilhado` não tem regra de negócio e não importa nada dos módulos.

### Nomes

| O quê | Padrão | Exemplo |
|---|---|---|
| Entidade | singular, PascalCase | `Insumo`, `CategoriaInsumo` |
| Tabela / coluna | snake_case (igual ao DER / SCRUM-111) | `categoria_insumo`, `quantidade_atual` |
| DTO de entrada | `<Entidade>Request` | `InsumoRequest` |
| DTO de saída | `<Entidade>Response` | `InsumoResponse` |
| Service / Repository / Controller | `<Entidade>Service` ... | `InsumoService` |
| Código | português, sem acento | `custoUnitario`, `estoqueMinimo` |

O Spring converte sozinho `quantidadeAtual` → `quantidade_atual`, então só use `@Column(name = ...)` quando o nome do DER for diferente.

### Tipos

- Dinheiro e quantidades: **`BigDecimal`** (nunca `double`).
- Data e hora: `LocalDateTime`; só data: `LocalDate`.
- Enums: `@Enumerated(EnumType.STRING)` **junto com** `@JdbcTypeCode(SqlTypes.VARCHAR)` (as colunas são `VARCHAR`; sem isso o Hibernate espera `ENUM` no MySQL e a aplicação não sobe).
- Booleanos: `Boolean`/`boolean` no Java ↔ `BIT(1)` no banco.
- IDs: `Long` com `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Relacionamentos `@ManyToOne` com `fetch = FetchType.LAZY`.

### API REST

- Toda rota começa com **`/api/`** e usa o plural: `/api/insumos`, `/api/pratos/{id}`.
- `GET` consulta · `POST` cria (201) · `PUT` altera (200) · `DELETE` exclui (204).
- Inativar e ativar: `PATCH /api/<recurso>/{id}/inativar` e `/ativar` (não apaga nada).
- Listagens devolvem **`PaginaResponse`** (`compartilhado/dto`), com no máximo 50 itens por página:

```java
return PaginaResponse.de(repository.findAll(pageable).map(InsumoResponse::de));
```

### Erros

- Não encontrado: `throw new NaoEncontradoException("Insumo", id);` → 404
- Regra de negócio: `throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "ESTOQUE_INSUFICIENTE", "mensagem para o usuário");`
- O `codigo` é sempre em MAIÚSCULAS_COM_UNDERLINE e a `mensagem` é pronta para mostrar na tela.
- Nunca use `try/catch` só para devolver erro no controller: o `TratadorGlobalDeErros` já faz isso.

### Banco

- Tabelas só por script Flyway (`V<n>__descricao.sql`). Antes de criar um script, avise o grupo qual número vai usar.
- Se a entidade não bater com a tabela, a aplicação não sobe (`ddl-auto=validate`): confira com o DER antes do PR.

## Banco de dados (Flyway)

As tabelas são criadas por scripts em `src/main/resources/db/migration`, no padrão `V1__descricao.sql`, `V2__descricao.sql`...

- **Nunca edite um script que já foi para a `main`.** Para mudar algo, crie um script novo com o próximo número.
- O Hibernate está em `ddl-auto=validate`: ele só confere se as entidades batem com as tabelas.

## Decisões do protótipo (divergências do DER)

O banco segue o DER da 3ª entrega (Miro). Diferenças conscientes nesta entrega:

| # | Decisão | Motivo |
|---|---|---|
| 1 | **Sem lotes:** o saldo fica em `insumo.quantidade_atual` (coluna a mais). A tabela `lote` não é criada e `movimentacao_estoque.lote_id` fica sempre vazio, sem chave estrangeira | No DER o saldo fica nos lotes, mas só perecíveis têm lote; o saldo dos não perecíveis ficaria sem lugar. Lote/FEFO ficam para uma próxima entrega |
| 2 | **Ajuste sem aprovação:** `ajuste_estoque.status` nasce `EFETIVADO` e `aprovador_id` fica vazio (o `AGUARDANDO_APROVACAO` fica para quando houver login) | Não há login nem perfis no protótipo |
| 3 | **Usuário mínimo:** `usuario` só com `id` e `login`, com 1 usuário de demonstração (id = 1, login `demo`) | O módulo Usuários não entra no protótipo, mas `responsavel_id` é obrigatório |
| 4 | **`comanda_id` sem chave estrangeira** | O módulo Caixa não entra no protótipo |
| 5 | **Tipos SQL:** `numeric` → `DECIMAL(12,2)` (dinheiro), `DECIMAL(12,3)` (quantidades) e `DECIMAL(12,4)` (`custo_unitario`); `boolean` → `BIT(1)`; `timestamp` → `DATETIME(6)`; enums → `VARCHAR` | `numeric` sem precisão perde casas decimais no MySQL; custo por grama (R$ 0,038/g) precisa de 4 casas; tipos escolhidos para bater com o Hibernate (`ddl-auto=validate`) |
| 6 | **Venda simulada:** a venda gera movimentações `BAIXA_POR_VENDA` sem comanda (`comanda_id` vazio) | O módulo Caixa ainda não existe |

Valores dos enums (iguais ao diagrama de classes do Miro):

| Enum | Valores | Onde fica |
|---|---|---|
| `UnidadeMedida` | `KG, G, L, ML, UNIDADE` | coluna `insumo.unidade_medida` |
| `TipoMovimentacaoEstoque` | `ENTRADA, RETIRADA, AJUSTE, BAIXA_POR_VENDA` | coluna `movimentacao_estoque.tipo` |
| `MotivoAjuste` | `PERDA, QUEBRA, VENCIMENTO, ERRO_CONTAGEM` | coluna `ajuste_estoque.motivo` |
| `StatusAjuste` | `AGUARDANDO_APROVACAO, EFETIVADO, CANCELADO` | coluna `ajuste_estoque.status` |
| `NivelEstoque` | `SUFICIENTE, ATENCAO, REPOR` | **não é coluna**: calculado por `Insumo.nivelEstoque()` |

**Como cada operação vira registro:**
- Entrada de mercadoria → `movimentacao_estoque` tipo `ENTRADA`
- Retirada manual → `movimentacao_estoque` tipo `RETIRADA`
- Baixa automática por venda → `movimentacao_estoque` tipo `BAIXA_POR_VENDA`
- Perda, quebra, vencimento ou erro de contagem → `movimentacao_estoque` tipo `AJUSTE` **+** uma linha em `ajuste_estoque` com o `motivo` (RN-EST-07)

Ficam fora desta entrega: `lote`, `complemento`, `prato_complemento`, `promocao`, `promocao_prato`, `historico_preco`, `comanda`, `item_comanda` e as tabelas de equipamentos.

## Dados da demo (V3)

O script `V3__dados_demo.sql` carrega os mesmos dados das telas do Figma:

- 5 categorias do cardápio: Lanches, Porções, Pizzas, Saladas, Sobremesas
- 5 insumos: Pão brioche, Carne bovina, Queijo cheddar, Alface americana, Molho da casa
- 6 pratos; só o **Hambúrguer artesanal** tem ficha técnica (custo R$ 8,40, CMV 26,25%)
- 1 `ENTRADA` por insumo no histórico, feita pelo usuário de demonstração

| Insumo | Unidade | Saldo | Mínimo | Custo unitário | Na ficha do hambúrguer |
|---|---|---|---|---|---|
| Pão brioche | `UNIDADE` | 10 | 5 | 1,20 | 1 |
| Carne bovina | `G` | 2000 | 1000 | 0,038 | 150 |
| Queijo cheddar | `UNIDADE` | 40 | 10 | 0,45 | 2 |
| Alface americana | `G` | 500 | 200 | 0,015 | 20 |
| Molho da casa | `ML` | 1000 | 300 | 0,010 | 30 |

### Conferir os dados no banco

Depois de rodar o backend uma vez, no MySQL Workbench:

```sql
SELECT nome, unidade_medida, quantidade_atual, estoque_minimo FROM insumo;   -- 5 insumos
SELECT nome, preco_venda, taxa_cmv FROM prato;                              -- 6 pratos
SELECT SUM(i.quantidade * ins.custo_unitario) AS custo
FROM item_ficha_tecnica i JOIN insumo ins ON ins.id = i.insumo_id;          -- 8.40
SELECT tipo, quantidade FROM movimentacao_estoque;                          -- 5 ENTRADA
```

### Roteiro da demo

1. Listar insumos: aparecem os 5 insumos com saldo e nível de estoque
2. Abrir o Hambúrguer artesanal: a ficha técnica mostra 5 insumos e custo **R$ 8,40**
3. Vender **2** hambúrgueres: a venda passa e o Pão brioche cai de 10 para **8**; o histórico ganha 5 movimentações `BAIXA_POR_VENDA`
4. Vender **12** hambúrgueres: a API responde 422 `ESTOQUE_INSUFICIENTE` (precisa de 12 pães e há 8) e **nenhum** saldo muda
5. Vender um prato sem ficha (ex.: Batata frita): a API responde 422 `PRATO_SEM_FICHA`

### Voltar o banco ao estado inicial

Depois de testar, para recomeçar a demo do zero, apague o banco no Workbench e rode o backend de novo (o Flyway recria tudo com V1, V2 e V3):

```sql
DROP DATABASE zentry;
```

> Isso apaga **todos** os dados do seu banco local. Não afeta ninguém do grupo.

## Formato de erro

Toda resposta de erro da API segue este formato:

```json
{
  "status": 404,
  "codigo": "NAO_ENCONTRADO",
  "mensagem": "Insumo com id 5 não encontrado.",
  "campos": [],
  "timestamp": "2026-11-16T23:00:00"
}
```

Nos services, basta lançar a exceção; o `TratadorGlobalDeErros` monta a resposta:

```java
throw new NaoEncontradoException("Insumo", id);                       // 404
throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,      // 422
        "ESTOQUE_INSUFICIENTE", "Estoque insuficiente para: Pão.");
```

Erros de validação (`@Valid`) voltam com status 400, código `VALIDACAO` e a lista de `campos` inválidos.
Uma rota que não existe (ex.: `GET /api/rota-errada`) volta com status 404 e código `ROTA_NAO_ENCONTRADA`.

## API

Contrato de cada recurso. Quem cria uma API acrescenta a sua parte aqui, no mesmo PR.

### Insumos (SCRUM-97)

| Método | Rota | O que faz | Resposta |
|---|---|---|---|
| `GET` | `/api/insumos?page=0&size=20&nome=pão&ativo=true` | Lista paginada, ordenada por nome (`nome` e `ativo` são opcionais) | 200 `PaginaResponse<InsumoResponse>` |
| `GET` | `/api/insumos/{id}` | Busca um insumo | 200 `InsumoResponse` |
| `POST` | `/api/insumos` | Cadastra | 201 `InsumoResponse` |
| `PUT` | `/api/insumos/{id}` | Edita | 200 `InsumoResponse` |
| `PATCH` | `/api/insumos/{id}/inativar` | Inativa (não apaga) | 204 |
| `PATCH` | `/api/insumos/{id}/ativar` | Reativa | 204 |
| `GET` | `/api/categorias-insumo` | Categorias para o select do formulário (sem paginação) | 200 `[{ "id": 1, "nome": "Carnes" }]` |

**Entrada (`InsumoRequest`, POST e PUT):**

```json
{
  "nome": "Pão brioche",
  "sku": "INS-001",
  "unidadeMedida": "UNIDADE",
  "perecivel": true,
  "estoqueMinimo": 5,
  "custoUnitario": 1.20,
  "controlaEstoque": true,
  "categoriaInsumoId": 1,
  "quantidadeInicial": 10
}
```

- Obrigatórios: `nome` (até 100), `unidadeMedida`, `perecivel`, `estoqueMinimo`, `custoUnitario`, `controlaEstoque`, `categoriaInsumoId`
- `sku` é opcional (até 50) e único; números não podem ser negativos; `custoUnitario` aceita até 4 casas decimais
- `quantidadeInicial` vale **só no POST** (vazio = 0). No PUT é ignorada: depois do cadastro o saldo só muda por movimentação (RN-EST-02)

**Saída (`InsumoResponse`):**

```json
{
  "id": 1,
  "nome": "Pão brioche",
  "sku": "INS-001",
  "unidadeMedida": "UNIDADE",
  "perecivel": true,
  "estoqueMinimo": 5.000,
  "custoUnitario": 1.2000,
  "controlaEstoque": true,
  "ativo": true,
  "quantidadeAtual": 10.000,
  "nivelEstoque": "SUFICIENTE",
  "categoria": { "id": 1, "nome": "Pães e massas" }
}
```

**Nível de estoque (RN-EST-03), decidido pelo grupo:** `REPOR` se saldo ≤ mínimo; `ATENCAO` se saldo ≤ 1,5 × mínimo; senão `SUFICIENTE`. Se o insumo não controla estoque, `nivelEstoque` vem `null`.

**Erros:**

| Quando | Status | `codigo` |
|---|---|---|
| Campo inválido ou faltando | 400 | `VALIDACAO` (lista em `campos`) |
| Valor de enum inválido ou JSON malformado | 400 | `CORPO_INVALIDO` |
| Insumo não existe | 404 | `NAO_ENCONTRADO` |
| Categoria informada não existe | 422 | `CATEGORIA_NAO_ENCONTRADA` |
| SKU já usado por outro insumo | 409 | `SKU_JA_CADASTRADO` |

**Contrato interno (para outros tickets):**

- Entidade `Insumo` (usada pela movimentação, SCRUM-99, mesmo módulo):
  - `aumentarSaldo(quantidade)` e `diminuirSaldo(quantidade)`: a quantidade tem que ser maior que zero (senão 422 `QUANTIDADE_INVALIDA`); `diminuirSaldo` lança 422 `ESTOQUE_INSUFICIENTE` se faltar saldo
  - `nivelEstoque()`, `inativar()`, `ativar()`
- `InsumoService.buscarPorId(id)`: devolve a entidade ou lança 404. É o que a ficha técnica (SCRUM-103) e a venda (SCRUM-105) usam

**Regras para a movimentação (SCRUM-99) não quebrar o cadastro:**

1. O `MovimentacaoService` busca o insumo pelo **`InsumoRepository`** (mesmo módulo), **não** pelo `InsumoService`. Se os dois services se chamarem, o Spring acusa dependência circular e a aplicação não sobe
2. O `MovimentacaoService` deve ter `registrarEntrada(insumo, quantidade)`
3. O `InsumoService.cadastrar` chama `registrarEntrada` quando `quantidadeInicial > 0`, dentro da mesma transação (feito no SCRUM-99)

### Movimentações de estoque (SCRUM-99)

UC-EST-001 e UC-EST-003. RF-EST-02, RF-EST-03, RF-EST-05, RF-EST-06, RN-EST-01, RN-EST-02, RN-EST-07.

| Método | Rota | O que faz | Resposta |
|---|---|---|---|
| `POST` | `/api/movimentacoes-estoque` | Registra uma entrada ou retirada | 201 `MovimentacaoEstoqueResponse` |
| `POST` | `/api/movimentacoes-estoque/ajustes` | Ajuste pela contagem física (perda, quebra, vencimento, erro de contagem) | 201 `MovimentacaoEstoqueResponse` |
| `GET` | `/api/movimentacoes-estoque/insumos/{insumoId}?page=0&size=20` | Histórico do insumo, paginado, do mais recente para o mais antigo | 200 `PaginaResponse<MovimentacaoEstoqueResponse>` |

**Entrada ou retirada (`MovimentacaoEstoqueRequest`):**

```json
{ "insumoId": 1, "tipo": "ENTRADA", "quantidade": 5 }
```

- `tipo` aceita só `ENTRADA` (soma ao saldo) ou `RETIRADA` (subtrai). `AJUSTE` tem rota própria e `BAIXA_POR_VENDA` só é gerado pela venda
- `quantidade` é obrigatória, maior que zero, com até 3 casas decimais

**Ajuste (`AjusteEstoqueRequest`):**

```json
{ "insumoId": 1, "quantidadeContada": 8, "motivo": "PERDA" }
```

- Informa a **contagem física**; o saldo do insumo passa a ser exatamente essa quantidade, para mais ou para menos
- `motivo` é obrigatório (RN-EST-07): `PERDA`, `QUEBRA`, `VENCIMENTO` ou `ERRO_CONTAGEM`
- Grava uma movimentação `AJUSTE` com o tamanho da diferença e uma linha em `ajuste_estoque` com `saldoSistema`, `quantidadeContada`, `diferenca` (com sinal), `motivo` e status `EFETIVADO`
- Exemplo: havia 10 pães e 2 estragaram → contagem 8, motivo `PERDA` → movimentação `AJUSTE` de 2, `diferenca` -2, saldo 8

**Saída (`MovimentacaoEstoqueResponse`):**

```json
{
  "id": 7,
  "tipo": "AJUSTE",
  "quantidade": 2.000,
  "dataHora": "2026-11-19T15:30:00.123456",
  "motivo": "PERDA"
}
```

`motivo` só vem preenchido quando o tipo é `AJUSTE`. O responsável é sempre o usuário de demonstração (id 1).

**Erros:**

| Quando | Status | `codigo` |
|---|---|---|
| Campo inválido ou faltando (inclusive ajuste sem motivo) | 400 | `VALIDACAO` (lista em `campos`) |
| Valor de enum inválido ou JSON malformado | 400 | `CORPO_INVALIDO` |
| Tipo `AJUSTE` ou `BAIXA_POR_VENDA` na rota de entrada/retirada | 422 | `TIPO_INVALIDO` |
| Insumo informado no corpo não existe | 422 | `INSUMO_NAO_ENCONTRADO` |
| Insumo do histórico não existe | 404 | `NAO_ENCONTRADO` |
| Retirada ou ajuste que deixaria o saldo negativo | 422 | `ESTOQUE_INSUFICIENTE` |
| Contagem igual ao saldo do sistema (ajuste sem diferença) | 422 | `QUANTIDADE_INVALIDA` |

**Contrato interno (para outros tickets):**

- `MovimentacaoEstoqueService.registrarEntrada(insumo, quantidade)`: soma ao saldo e grava `ENTRADA`. Usado pelo cadastro de insumo
- `MovimentacaoEstoqueService.registrarBaixa(insumo, quantidade)`: subtrai do saldo e grava `BAIXA_POR_VENDA`; lança 422 `ESTOQUE_INSUFICIENTE` se faltar saldo. É o que a venda (SCRUM-105) usa
- `UsuarioService.buscarPorId(id)` (pacote `usuario`): só o mínimo para gravar o responsável

**Pendente de decisão do grupo:** os docs não dizem se insumo **inativo** ou que **não controla estoque** pode ser movimentado. Por enquanto a API permite os dois.

### Pratos (SCRUM-101)

| Método | Rota | O que faz | Resposta |
|---|---|---|---|
| `GET` | `/api/pratos?page=0&size=20&nome=hamb&categoriaId=1&ativo=true` | Lista paginada, ordenada por nome (`nome`, `categoriaId` e `ativo` são opcionais) | 200 `PaginaResponse<PratoResponse>` |
| `GET` | `/api/pratos/{id}` | Busca um prato | 200 `PratoResponse` |
| `POST` | `/api/pratos` | Cadastra (o prato nasce ativo) | 201 `PratoResponse` |
| `PUT` | `/api/pratos/{id}` | Edita (não muda o status) | 200 `PratoResponse` |
| `PATCH` | `/api/pratos/{id}/inativar` | Inativa (não apaga), RF-CAR-003 | 204 |
| `PATCH` | `/api/pratos/{id}/ativar` | Reativa | 204 |

Não há `DELETE`: pela RN-CAR-001 só se exclui prato sem pedidos, e no protótipo o prato é apenas inativado.

**Entrada (`PratoRequest`, POST e PUT):**

```json
{
  "nome": "Hambúrguer artesanal",
  "descricao": "Pão brioche, 150 g de carne, cheddar, alface e molho da casa",
  "precoVenda": 32.00,
  "categoriaCardapioId": 1
}
```

- Obrigatórios: `nome` (até 100), `precoVenda` (≥ 0, até 2 casas decimais) e `categoriaCardapioId`
- `descricao` é opcional (até 500); texto em branco vira `null`
- A categoria precisa existir **e estar ativa** (decisão do grupo: prato novo ou editado não entra em categoria inativa)
- `taxa_cmv` e `imagem_url` existem na tabela, mas ficam fora desta entrega e não aparecem na API

**Saída (`PratoResponse`):**

```json
{
  "id": 1,
  "nome": "Hambúrguer artesanal",
  "descricao": "Pão brioche, 150 g de carne, cheddar, alface e molho da casa",
  "precoVenda": 32.00,
  "ativo": true,
  "categoria": { "id": 1, "nome": "Lanches", "ativa": true }
}
```

**Erros:**

| Quando | Status | `codigo` |
|---|---|---|
| Campo inválido ou faltando | 400 | `VALIDACAO` (lista em `campos`) |
| JSON malformado | 400 | `CORPO_INVALIDO` |
| Prato não existe | 404 | `NAO_ENCONTRADO` |
| Categoria informada não existe | 422 | `CATEGORIA_NAO_ENCONTRADA` |
| Categoria informada está inativa | 422 | `CATEGORIA_INATIVA` |

**Contrato interno (para outros tickets):**

- `PratoService.buscarPorId(id)`: devolve a entidade `Prato` ou lança 404. É o que a ficha técnica (SCRUM-103) e a venda (SCRUM-105) usam
- Entidade `Prato`: `getAtivo()`, `inativar()`, `ativar()`. A venda deve recusar prato inativo
- `CategoriaCardapio` e `CategoriaCardapioRepository` foram criados aqui só com o mínimo que o prato precisa. O CRUD da categoria é do **SCRUM-95** (seção abaixo), que usa essa mesma entidade

### Categorias do cardápio (SCRUM-95)

UC-CAR-002. RF-CAR-006 a RF-CAR-009, RN-CAR-002.

| Método | Rota | O que faz | Resposta |
|---|---|---|---|
| `GET` | `/api/categorias-cardapio?page=0&size=20` | Lista paginada, ordenada por nome | 200 `PaginaResponse<CategoriaCardapioResponse>` |
| `GET` | `/api/categorias-cardapio/{id}` | Busca uma categoria | 200 `CategoriaCardapioResponse` |
| `POST` | `/api/categorias-cardapio` | Cadastra (a categoria nasce ativa) | 201 `CategoriaCardapioResponse` |
| `PUT` | `/api/categorias-cardapio/{id}` | Edita o nome (não muda o status) | 200 `CategoriaCardapioResponse` |
| `PATCH` | `/api/categorias-cardapio/{id}/inativar` | Inativa, RF-CAR-008 | 204 |
| `PATCH` | `/api/categorias-cardapio/{id}/ativar` | Reativa | 204 |
| `DELETE` | `/api/categorias-cardapio/{id}` | Exclui definitivamente, só sem prato vinculado (RF-CAR-009, RN-CAR-002) | 204 |

**Entrada (`CategoriaCardapioRequest`, POST e PUT):**

```json
{ "nome": "Bebidas" }
```

- `nome` é obrigatório (até 100) e único. A comparação segue o banco, que não diferencia maiúsculas nem acentos ("lanches" bate com "Lanches")

**Saída (`CategoriaCardapioResponse`):**

```json
{ "id": 1, "nome": "Lanches", "ativa": true }
```

O mesmo formato aparece no campo `categoria` do `PratoResponse`.

**Erros:**

| Quando | Status | `codigo` |
|---|---|---|
| Nome vazio ou com mais de 100 caracteres | 400 | `VALIDACAO` (lista em `campos`) |
| JSON malformado | 400 | `CORPO_INVALIDO` |
| Categoria não existe | 404 | `NAO_ENCONTRADO` |
| Nome já usado por outra categoria | 409 | `CATEGORIA_JA_CADASTRADA` |
| Exclusão de categoria com prato vinculado | 409 | `CATEGORIA_COM_PRATOS` |

### Ficha técnica e custo do prato (SCRUM-103)

UC-CAR-003. RF-CAR-010, RF-CAR-011, RF-CAR-012.

| Método | Rota | O que faz | Resposta |
|---|---|---|---|
| `GET` | `/api/pratos/{pratoId}/ficha-tecnica` | Busca a ficha do prato (cria uma vazia se ainda não existir) | 200 `FichaTecnicaResponse` |
| `POST` | `/api/pratos/{pratoId}/ficha-tecnica/itens` | Adiciona um insumo à ficha | 201 `FichaTecnicaResponse` |
| `PUT` | `/api/pratos/{pratoId}/ficha-tecnica/itens/{itemId}` | Altera a quantidade de um item | 200 `FichaTecnicaResponse` |
| `DELETE` | `/api/pratos/{pratoId}/ficha-tecnica/itens/{itemId}` | Remove um item | 200 `FichaTecnicaResponse` |

Todas as rotas devolvem a ficha inteira, com o custo já recalculado. O `DELETE` responde 200 (e não 204) porque a tela precisa do novo custo total.

**Entrada:**

```json
{ "insumoId": 1, "quantidade": 150 }
```

- `POST` usa `ItemFichaRequest` (`insumoId` e `quantidade`); `PUT` usa `QuantidadeItemRequest` (só `quantidade`)
- `quantidade` é obrigatória, maior que zero, com até 3 casas decimais, **na unidade do insumo** (sem conversão)
- O mesmo insumo não pode aparecer duas vezes na mesma ficha

**Saída (`FichaTecnicaResponse`), Hambúrguer artesanal com os dados do V3:**

```json
{
  "pratoId": 1,
  "pratoNome": "Hambúrguer artesanal",
  "categoriaNome": "Lanches",
  "precoVenda": 32.00,
  "custoTotal": 8.40,
  "atualizadaEm": "2026-11-19T18:00:00.123456",
  "itens": [
    { "id": 1, "insumoId": 1, "insumoNome": "Pão brioche", "unidadeMedida": "UNIDADE", "custoUnitario": 1.2000, "quantidade": 1, "custo": 1.2000 },
    { "id": 2, "insumoId": 2, "insumoNome": "Carne bovina", "unidadeMedida": "G", "custoUnitario": 0.0380, "quantidade": 150, "custo": 5.7000 }
  ]
}
```

(lista resumida; a ficha real traz os 5 insumos)

**Regras:**

- Custo do item = `quantidade × custoUnitario` do insumo (4 casas); custo da ficha = soma dos itens, gravado com 2 casas em `ficha_tecnica.custo_total`
- Toda alteração na ficha atualiza `custo_total` e `atualizada_em`

**Erros:**

| Quando | Status | `codigo` |
|---|---|---|
| `insumoId` ou `quantidade` faltando, quantidade ≤ 0 ou com mais de 3 casas | 400 | `VALIDACAO` (lista em `campos`) |
| Prato, insumo ou item da ficha não existe | 404 | `NAO_ENCONTRADO` |
| Insumo já está na ficha | 409 | `INSUMO_JA_NA_FICHA` |

**Contrato interno (para outros tickets):**

- `FichaTecnicaService.listarItensDoPrato(pratoId)`: devolve os itens (`ItemFichaTecnica`, com `getInsumo()` e `getQuantidade()`) ou lista vazia se o prato não tem ficha ou a ficha está sem itens. É o que a venda (SCRUM-105) usa. Chamar dentro de uma transação, pois o insumo é carregado sob demanda

### Venda e baixa de estoque (SCRUM-105)

| Método | Rota | O que faz | Resposta |
|---|---|---|---|
| `POST` | `/api/pratos/{pratoId}/vendas` | Registra a venda de N unidades e dá baixa nos insumos da ficha técnica (UC-CAR-006) | 201 `VendaResponse` |

Venda simulada: não existe tabela de venda nem comanda. Cada insumo baixado vira uma movimentação `BAIXA_POR_VENDA` (com `comanda_id` vazio e o usuário de demonstração id 1). Quando o Caixa existir, ele chama o mesmo `VendaService.registrarVenda` ao fechar a comanda.

**Entrada (`VendaRequest`):**

```json
{ "quantidade": 2 }
```

- `quantidade` obrigatória, de 1 a 999

**Saída (`VendaResponse`), vendendo 2 Hambúrgueres artesanais com os dados do V3:**

```json
{
  "pratoId": 1,
  "prato": "Hambúrguer artesanal",
  "quantidade": 2,
  "baixas": [
    { "insumoId": 1, "insumo": "Pão brioche", "unidadeMedida": "UNIDADE", "quantidadeBaixada": 2, "saldoAtual": 8 },
    { "insumoId": 2, "insumo": "Carne bovina", "unidadeMedida": "G", "quantidadeBaixada": 300, "saldoAtual": 1700 }
  ]
}
```

(lista resumida; a venda real traz os 5 insumos da ficha)

**Regras:**

- Para cada item da ficha, baixa `quantidade do item × quantidade vendida`, na unidade do insumo
- Insumo com `controlaEstoque = false` é pulado e não aparece em `baixas` (RN-CAR-006)
- Tudo numa transação: se faltar saldo de **qualquer** insumo, a venda inteira é recusada e nada é baixado (RNF-CAR-005). A mensagem lista todos os insumos sem saldo

**Erros:**

| Quando | Status | `codigo` |
|---|---|---|
| `quantidade` faltando, zero, negativa ou acima de 999 | 400 | `VALIDACAO` |
| Prato não existe | 404 | `NAO_ENCONTRADO` |
| Prato inativo | 422 | `PRATO_INATIVO` |
| Prato sem ficha técnica (ou ficha vazia) | 422 | `PRATO_SEM_FICHA` |
| Falta saldo de algum insumo | 422 | `ESTOQUE_INSUFICIENTE` |

**Depende de:** `FichaTecnicaService.listarItensDoPrato(pratoId)` (SCRUM-103) e `MovimentacaoEstoqueService.registrarBaixa(insumo, quantidade)` (SCRUM-99)
