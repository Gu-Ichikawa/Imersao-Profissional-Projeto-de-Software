import api, { type Pagina } from '../../../compartilhado/servicos/api'

// Formatos iguais aos DTOs do backend (SCRUM-101)
export type CategoriaCardapio = {
  id: number
  nome: string
}

export type Prato = {
  id: number
  nome: string
  descricao: string | null
  precoVenda: number
  ativo: boolean
  categoria: CategoriaCardapio
}

export type PratoEntrada = {
  nome: string
  descricao?: string
  precoVenda: number
  categoriaCardapioId: number
}

export type FiltrosPrato = {
  nome?: string
  categoriaId?: number
  ativo?: boolean
}

// "pagina" começa em 1 (tabela do Ant Design); o backend começa em 0
export async function listarPratos(filtros: FiltrosPrato, pagina: number, tamanho: number) {
  const resposta = await api.get<Pagina<Prato>>('/pratos', {
    params: { ...filtros, page: pagina - 1, size: tamanho, sort: 'nome' },
  })
  return resposta.data
}

export async function cadastrarPrato(dados: PratoEntrada) {
  const resposta = await api.post<Prato>('/pratos', dados)
  return resposta.data
}

export async function editarPrato(id: number, dados: PratoEntrada) {
  const resposta = await api.put<Prato>(`/pratos/${id}`, dados)
  return resposta.data
}

export async function inativarPrato(id: number) {
  await api.patch(`/pratos/${id}/inativar`)
}

export async function ativarPrato(id: number) {
  await api.patch(`/pratos/${id}/ativar`)
}

// Categorias para os selects (API de categoria, SCRUM-95). Até 50, o máximo por página da API
export async function listarCategorias(): Promise<CategoriaCardapio[]> {
  const resposta = await api.get<Pagina<CategoriaCardapio>>('/categorias-cardapio', {
    params: { size: 50, sort: 'nome' },
  })
  return resposta.data.content
}
