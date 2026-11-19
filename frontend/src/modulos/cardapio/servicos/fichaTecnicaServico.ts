import api, { type Pagina } from '../../../compartilhado/servicos/api'

export type UnidadeMedida = 'KG' | 'G' | 'L' | 'ML' | 'UNIDADE'

export type ItemFicha = {
  id: number
  insumoId: number
  insumoNome: string
  unidadeMedida: UnidadeMedida
  custoUnitario: number
  quantidade: number
  custo: number
}

export type FichaTecnica = {
  pratoId: number
  pratoNome: string
  categoriaNome: string
  precoVenda: number
  custoTotal: number
  itens: ItemFicha[]
}

// Só o que a tela precisa de cada insumo para o seletor
export type InsumoOpcao = {
  id: number
  nome: string
  unidadeMedida: UnidadeMedida
  custoUnitario: number
}

// TODO (SCRUM-103): rotas combinadas para a API de ficha técnica.
export async function buscarFicha(pratoId: number): Promise<FichaTecnica> {
  const resposta = await api.get<FichaTecnica>(`/pratos/${pratoId}/ficha-tecnica`)
  return resposta.data
}

export async function adicionarItem(pratoId: number, insumoId: number, quantidade: number): Promise<FichaTecnica> {
  const resposta = await api.post<FichaTecnica>(`/pratos/${pratoId}/ficha-tecnica/itens`, { insumoId, quantidade })
  return resposta.data
}

export async function alterarQuantidade(pratoId: number, itemId: number, quantidade: number): Promise<FichaTecnica> {
  const resposta = await api.put<FichaTecnica>(`/pratos/${pratoId}/ficha-tecnica/itens/${itemId}`, { quantidade })
  return resposta.data
}

export async function removerItem(pratoId: number, itemId: number): Promise<FichaTecnica> {
  const resposta = await api.delete<FichaTecnica>(`/pratos/${pratoId}/ficha-tecnica/itens/${itemId}`)
  return resposta.data
}

// Insumos ativos para escolher na lista (API de insumo, SCRUM-97)
export async function listarInsumosAtivos(): Promise<InsumoOpcao[]> {
  const resposta = await api.get<Pagina<InsumoOpcao>>('/insumos', { params: { ativo: true, size: 50 } })
  return resposta.data.content.map(({ id, nome, unidadeMedida, custoUnitario }) => ({ id, nome, unidadeMedida, custoUnitario }))
}
