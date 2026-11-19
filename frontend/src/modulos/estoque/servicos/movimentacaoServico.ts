import api, { type Pagina } from '../../../compartilhado/servicos/api'

export type TipoMovimentacao = 'ENTRADA' | 'RETIRADA' | 'AJUSTE' | 'BAIXA_POR_VENDA'
export type MotivoAjuste = 'PERDA' | 'QUEBRA' | 'VENCIMENTO' | 'ERRO_CONTAGEM'

export const MOTIVOS: MotivoAjuste[] = ['PERDA', 'QUEBRA', 'VENCIMENTO', 'ERRO_CONTAGEM']

// Usados no formulário e no histórico
export const ROTULOS_TIPO: Record<TipoMovimentacao, string> = {
  ENTRADA: 'Entrada',
  RETIRADA: 'Retirada',
  AJUSTE: 'Ajuste',
  BAIXA_POR_VENDA: 'Venda',
}

export const ROTULOS_MOTIVO: Record<MotivoAjuste, string> = {
  PERDA: 'Perda',
  QUEBRA: 'Quebra',
  VENCIMENTO: 'Vencimento',
  ERRO_CONTAGEM: 'Erro de contagem',
}

export type Movimentacao = {
  id: number
  tipo: TipoMovimentacao
  quantidade: number
  dataHora: string
  motivo: MotivoAjuste | null // só vem preenchido quando o tipo é AJUSTE
}

// BAIXA_POR_VENDA só é gerado pela venda: a rota de entrada/retirada não aceita
export type MovimentacaoRequest = {
  insumoId: number
  tipo: 'ENTRADA' | 'RETIRADA'
  quantidade: number
}

export type AjusteRequest = {
  insumoId: number
  quantidadeContada: number
  motivo: MotivoAjuste
}

export async function registrarMovimentacao(dados: MovimentacaoRequest): Promise<Movimentacao> {
  const resposta = await api.post<Movimentacao>('/movimentacoes-estoque', dados)
  return resposta.data
}

export async function ajustarEstoque(dados: AjusteRequest): Promise<Movimentacao> {
  const resposta = await api.post<Movimentacao>('/movimentacoes-estoque/ajustes', dados)
  return resposta.data
}

// "pagina" começa em 1 (tabela do Ant Design); a API começa em 0
export async function listarHistorico(insumoId: number, pagina: number, tamanho: number): Promise<Pagina<Movimentacao>> {
  const resposta = await api.get<Pagina<Movimentacao>>(`/movimentacoes-estoque/insumos/${insumoId}`, {
    params: { page: pagina - 1, size: tamanho },
  })
  return resposta.data
}
