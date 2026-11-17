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
- Enums: `@Enumerated(EnumType.STRING)` (nunca a posição numérica).
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
