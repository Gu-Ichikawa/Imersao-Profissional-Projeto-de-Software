import axios from 'axios'
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

// Categorias para os selects. A rota /categorias-cardapio é do SCRUM-95.
// Enquanto ela não existir, o backend responde com erro (hoje 500, pelo tratador genérico)
// e as categorias são montadas a partir dos pratos cadastrados.
export async function listarCategorias(): Promise<CategoriaCardapio[]> {
  try {
    const resposta = await api.get<CategoriaCardapio[]>('/categorias-cardapio')
    return resposta.data
  } catch (erro) {
    if (axios.isAxiosError(erro) && erro.response) {
      const pratos = await listarPratos({}, 1, 50)
      const porId = new Map(pratos.content.map((prato) => [prato.categoria.id, prato.categoria]))
      return [...porId.values()].sort((a, b) => a.nome.localeCompare(b.nome))
    }
    throw erro
  }
}
