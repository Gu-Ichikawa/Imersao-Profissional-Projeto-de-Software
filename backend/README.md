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
