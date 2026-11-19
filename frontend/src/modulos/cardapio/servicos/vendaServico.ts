import api from '../../../compartilhado/servicos/api'

// TODO (SCRUM-105): confirmar a rota com a API de registrar venda.
// Erros esperados: 422 ESTOQUE_INSUFICIENTE, PRATO_INATIVO e PRATO_SEM_FICHA.
export async function registrarVenda(pratoId: number, quantidade: number): Promise<void> {
  await api.post(`/pratos/${pratoId}/vendas`, { quantidade })
}
