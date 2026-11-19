# ZENTRY — Frontend

Aplicação web do ZENTRY em **React + TypeScript**, criada com **Vite**, usando **Ant Design** (componentes), **React Router** (navegação) e **Axios** (chamadas à API).

## Pré-requisitos

- Node.js (versão LTS)
- Backend rodando em `http://localhost:8080` (ver `backend/README.md`)

## Instalar

```bash
cd frontend
npm install
```

## Configurar

Copie o arquivo de exemplo para criar o seu `.env` (ele não vai para o Git):

```bash
cp .env.example .env
```

| Variável | Padrão | Para quê |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080/api` | endereço da API do backend |

Sem o `.env`, o frontend usa o endereço padrão acima.

## Rodar

```bash
npm run dev
```

Abra `http://localhost:5173`. Mantenha essa porta: é a que o backend libera no CORS.

| Comando | O que faz |
|---|---|
| `npm run dev` | sobe o site em modo de desenvolvimento |
| `npm run lint` | confere o código com o ESLint |
| `npm run build` | compila o projeto (roda o TypeScript); use antes de abrir o PR |

## Estrutura

```
src/
├── main.tsx              → ponto de entrada: React Router + Ant Design (pt-BR e tema do Figma)
├── App.tsx               → rotas: qual endereço abre qual página
├── assets/               → imagens (logo)
├── compartilhado/        → o que todos os módulos usam
│   ├── layout/           → LayoutPrincipal (menu lateral + área da página)
│   ├── componentes/      → componentes reutilizáveis (ex.: CabecalhoPagina)
│   └── servicos/         → api.ts (cliente único da API)
└── modulos/              → um por módulo do backend
    ├── cardapio/
    │   ├── paginas/      → uma página por rota (ex.: PratosPage)
    │   ├── componentes/  → partes das páginas (formulários, modais)
    │   └── servicos/     → funções que chamam a API do módulo
    └── estoque/
        └── (mesma estrutura)
```

A organização espelha o backend (monólito modular): um módulo não importa nada de outro módulo; o que é comum fica em `compartilhado`.

## Rotas

| Endereço | Página | Responsável |
|---|---|---|
| `/` | redireciona para `/cardapio/pratos` | — |
| `/cardapio/categorias` | `CategoriasPage` | SCRUM-96 |
| `/cardapio/pratos` | `PratosPage` | SCRUM-102 (venda: SCRUM-106) |
| `/cardapio/pratos/:id/ficha-tecnica` | `FichaTecnicaPage` | SCRUM-104 |
| `/estoque/insumos` | `InsumosPage` | SCRUM-98 |
| `/estoque/movimentacoes` | `MovimentacoesPage` | SCRUM-100 |

Os módulos Salão, Caixa, Financeiro, Delivery e Usuários aparecem desativados no menu: não fazem parte do protótipo.

## Convenções

### Nomes e arquivos

| O quê | Padrão | Exemplo |
|---|---|---|
| Página | `<Nome>Page.tsx` em `modulos/<modulo>/paginas` | `PratosPage.tsx` |
| Componente | PascalCase em `componentes` | `ModalRegistrarVenda.tsx` |
| Serviço da API | `<recurso>Servico.ts` em `modulos/<modulo>/servicos` | `pratoServico.ts` |
| Código | português, sem acento | `buscarPratos`, `precoVenda` |

### Telas

- Toda página começa com o `CabecalhoPagina` (caminho, título e descrição), igual ao Figma.
- Use os componentes do Ant Design (`Table`, `Form`, `Modal`, `Button`...). Cores, fontes e cantos já vêm do tema no `main.tsx`: evite cores escritas à mão.
- Botões em formato de pílula: `<Button shape="round">`.

### Chamadas à API

- Sempre use o `api` de `compartilhado/servicos/api.ts` (nunca crie outro `axios`):

```ts
import api, { type Pagina } from '../../../compartilhado/servicos/api'

export async function listarPratos(pagina: number) {
  const resposta = await api.get<Pagina<Prato>>('/pratos', { params: { page: pagina - 1 } })
  return resposta.data
}
```

- **Paginação:** o backend conta as páginas a partir de **0** e a tabela do Ant Design a partir de **1** (por isso o `pagina - 1`). Os itens vêm em `content` e o total em `totalElements`.
- **Erros:** use `mensagemDeErro(erro)` para mostrar a mensagem do backend e `errosDeCampo(erro)` para marcar campos inválidos de um formulário (erro 400). Cada tela decide onde mostrar o erro.
- Os nomes dos campos no JSON são os mesmos do backend (`precoVenda`, `quantidadeAtual`...), e os enums chegam em MAIÚSCULAS (`UNIDADE`, `BAIXA_POR_VENDA`): mostre um texto legível na tela.

### Formatadores

Use sempre `compartilhado/formatadores.ts` (não crie outro):

| Função | Exemplo |
|---|---|
| `moeda(32)` | `R$ 32,00` |
| `moedaUnitaria(0.038)` | `R$ 0,0380` (custo de insumo: 4 casas só quando precisa) |
| `rotuloUnidade('UNIDADE')` | `un` (KG → kg, G → g, L → L, ML → ml) |

Erro geral de um modal fica no topo dele: fundo `#FBEAEA`, texto `#8E2B20`, cantos 10.

### Git

- Branch por ticket: `SCRUM-<número>-<descricao>`.
- Commits pequenos: `SCRUM-<número>: o que foi feito`.
- Antes do PR: `npm run lint` e `npm run build` sem erros, e `git merge origin/main`.

## Telas e rotas da API

Todas as telas já estão ligadas ao backend. O contrato completo de cada rota está em `backend/README.md`.

| Tela | Serviço | Rotas da API |
|---|---|---|
| Categorias (SCRUM-96) | `cardapio/servicos/categoriaServico.ts` | `/categorias-cardapio` |
| Pratos (SCRUM-102) | `cardapio/servicos/pratoServico.ts` | `/pratos` |
| Ficha técnica (SCRUM-104) | `cardapio/servicos/fichaTecnicaServico.ts` | `/pratos/{id}/ficha-tecnica` e `/pratos/{id}/ficha-tecnica/itens` |
| Registrar venda (SCRUM-106, modal aberto pelo botão "Vender" da tela de pratos) | `cardapio/servicos/vendaServico.ts` | `POST /pratos/{id}/vendas` |
| Insumos (SCRUM-98) | `estoque/servicos/insumoServico.ts` | `/insumos` |
| Movimentações (SCRUM-100) | `estoque/servicos/movimentacaoServico.ts` | `/movimentacoes-estoque` |
