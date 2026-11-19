# ZENTRY

Sistema de gestão para restaurantes, desenvolvido no projeto de Imersão Profissional (ADS).
O ZENTRY organiza cardápio, estoque, salão, caixa, financeiro, delivery e usuários em um só lugar.

Este repositório contém o **protótipo da Entrega 5 (MVP)**, com os módulos **Cardápio** e **Estoque**
e a baixa automática de insumos quando um prato é vendido.

## Escopo da Entrega 5

| Funcionalidade | Backend | Frontend |
|---|---|---|
| Insumos (cadastro, edição, inativação, nível de estoque) | ✅ | ✅ |
| Pratos (cadastro, edição, inativação) | ✅ | ✅ |
| Categorias do cardápio | Pendente | Pendente |
| Movimentações de estoque (entrada, retirada, ajuste) | ✅ | Pendente |
| Ficha técnica e custo do prato | Pendente | Tela pronta, aguarda a API |
| Registrar venda com baixa automática de estoque | Pendente | Tela pronta, aguarda a API |

> Atualize esta tabela ao concluir cada funcionalidade.

Os módulos Salão, Caixa, Financeiro, Delivery e Usuários fazem parte do produto, mas ficam fora deste protótipo
(aparecem desativados no menu).

## Tecnologias

| Camada | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Data JPA, Bean Validation, Flyway, Lombok |
| Banco de dados | MySQL 8 |
| Frontend | React, TypeScript, Vite, Ant Design, React Router, Axios |
| Organização | Jira (Scrum), GitHub (branch por ticket e Pull Request com revisão), Figma, Miro |

## Arquitetura

**Monólito modular**: uma única aplicação dividida em módulos de negócio (`cardapio`, `estoque`),
com o que é comum em `compartilhado`. Um módulo só usa outro pelo service público dele, nunca pelo repository.
O frontend espelha a mesma divisão por módulos.

O banco é criado e versionado pelo Flyway a partir do DER da 3ª entrega. As diferenças conscientes em relação
ao DER estão documentadas em [`backend/README.md`](backend/README.md#decisões-do-protótipo-divergências-do-der).

## Estrutura do repositório

```
├── backend/     → API REST (Spring Boot) e scripts do banco (Flyway)
├── frontend/    → aplicação web (React + Vite)
├── docs/        → documentos das entregas anteriores
└── index.html   → página do projeto (GitHub Pages)
```

## Como rodar

**Pré-requisitos:** Java 21 ou superior, MySQL 8 em `localhost:3306` e Node.js (LTS).

**1. Backend** (em um terminal):

```bash
cd backend
export DB_PASSWORD=sua_senha_do_mysql
./mvnw spring-boot:run
```

O banco `zentry` é criado na primeira execução, já com os dados da demonstração.
A API sobe em `http://localhost:8080/api` (aguarde `Started ZentryBackendApplication`).

**2. Frontend** (em outro terminal):

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`. Mantenha essa porta: é a liberada no CORS do backend.

Detalhes, variáveis de ambiente e testes:

- [`backend/README.md`](backend/README.md): configuração, convenções, decisões do banco, dados da demo e **contrato da API**
- [`frontend/README.md`](frontend/README.md): estrutura, rotas das telas e convenções

## Testes

```bash
cd backend && ./mvnw test           # testes do backend (precisa do MySQL ligado)
cd frontend && npm run lint && npm run build   # verificação do frontend
```

## Fluxo de trabalho

- Cada tarefa é um ticket no Jira (`SCRUM-<número>`) e tem a sua branch: `SCRUM-<número>-<descricao>`.
- Commits pequenos, no formato `SCRUM-<número>: o que foi feito`.
- Toda mudança entra na `main` por **Pull Request revisado por outra pessoa do grupo**.
- Scripts do banco que já estão na `main` não são editados: mudanças viram um novo script.

## Documentação do projeto

- [Requisitos e regras de negócio](<docs/Requisitos e Regras  - ZENTRY.pdf>)
- [System Design (4ª entrega)](<docs/4º Entrega - System Design - Zentry .pdf>)
- [Diagramas: DER, classes, casos de uso e sequência (Miro)](https://miro.com/app/board/uXjVEcDy31I=/)
- [Protótipo das telas (Figma)](https://www.figma.com/design/rnORqogK2eFLscsmwvS0pC/ZENTRY---Tela-Registrar-Venda)

## Equipe

| Integrante | Papel |
|---|---|
| João Marcos | Scrum Master / Product Owner e desenvolvimento |
| Gustavo Ichikawa | Desenvolvimento |
| Gustavo Dena | Desenvolvimento |
| Monique Muniz | Desenvolvimento |
| Stephany Christine | Desenvolvimento |
