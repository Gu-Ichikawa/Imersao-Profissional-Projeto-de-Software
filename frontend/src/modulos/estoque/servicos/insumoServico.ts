import api, { type Pagina } from '../../../compartilhado/servicos/api'

export type UnidadeMedida = 'KG' | 'G' | 'L' | 'ML' | 'UNIDADE'
export type NivelEstoque = 'SUFICIENTE' | 'ATENCAO' | 'REPOR'

export const UNIDADES: UnidadeMedida[] = ['KG', 'G', 'L', 'ML', 'UNIDADE']

export type CategoriaInsumo = {
  id: number
  nome: string
}

export type Insumo = {
  id: number
  nome: string
  sku: string | null
  unidadeMedida: UnidadeMedida
  perecivel: boolean
  estoqueMinimo: number
  custoUnitario: number
  controlaEstoque: boolean
  ativo: boolean
  quantidadeAtual: number
  nivelEstoque: NivelEstoque | null // null quando o insumo não controla estoque
  categoria: CategoriaInsumo
}

export type InsumoRequest = {
  nome: string
  sku: string | null
  unidadeMedida: UnidadeMedida
  perecivel: boolean
  estoqueMinimo: number
  custoUnitario: number
  controlaEstoque: boolean
  categoriaInsumoId: number
  quantidadeInicial?: number // só no cadastro
}

export type FiltrosInsumo = {
  nome?: string
  ativo?: boolean
}

// "pagina" começa em 1 (tabela do Ant Design); a API começa em 0
export async function listarInsumos(pagina: number, tamanho: number, filtros: FiltrosInsumo): Promise<Pagina<Insumo>> {
  const resposta = await api.get<Pagina<Insumo>>('/insumos', {
    params: { page: pagina - 1, size: tamanho, nome: filtros.nome, ativo: filtros.ativo },
  })
  return resposta.data
}

export async function cadastrarInsumo(dados: InsumoRequest): Promise<Insumo> {
  const resposta = await api.post<Insumo>('/insumos', dados)
  return resposta.data
}

export async function editarInsumo(id: number, dados: InsumoRequest): Promise<Insumo> {
  const resposta = await api.put<Insumo>(`/insumos/${id}`, dados)
  return resposta.data
}

export async function inativarInsumo(id: number): Promise<void> {
  await api.patch(`/insumos/${id}/inativar`)
}

export async function ativarInsumo(id: number): Promise<void> {
  await api.patch(`/insumos/${id}/ativar`)
}

export async function listarCategoriasInsumo(): Promise<CategoriaInsumo[]> {
  const resposta = await api.get<CategoriaInsumo[]>('/categorias-insumo')
  return resposta.data
}
