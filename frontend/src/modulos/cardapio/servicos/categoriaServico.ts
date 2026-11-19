import api, { type Pagina } from '../../../compartilhado/servicos/api'

// Formatos iguais aos DTOs do backend (SCRUM-95)
export type Categoria = {
  id: number
  nome: string
  ativa: boolean
}

export type CategoriaEntrada = {
  nome: string
}

// "pagina" começa em 1 (tabela do Ant Design); o backend começa em 0
export async function listarCategoriasPaginadas(pagina: number, tamanho: number) {
  const resposta = await api.get<Pagina<Categoria>>('/categorias-cardapio', {
    params: { page: pagina - 1, size: tamanho, sort: 'nome' },
  })
  return resposta.data
}

export async function cadastrarCategoria(dados: CategoriaEntrada) {
  const resposta = await api.post<Categoria>('/categorias-cardapio', dados)
  return resposta.data
}

export async function editarCategoria(id: number, dados: CategoriaEntrada) {
  const resposta = await api.put<Categoria>(`/categorias-cardapio/${id}`, dados)
  return resposta.data
}

export async function inativarCategoria(id: number) {
  await api.patch(`/categorias-cardapio/${id}/inativar`)
}

export async function ativarCategoria(id: number) {
  await api.patch(`/categorias-cardapio/${id}/ativar`)
}

// 409 CATEGORIA_COM_PRATOS quando há prato vinculado (RN-CAR-002)
export async function excluirCategoria(id: number) {
  await api.delete(`/categorias-cardapio/${id}`)
}
