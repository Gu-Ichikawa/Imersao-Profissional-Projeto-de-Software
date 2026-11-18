import axios from 'axios'

// Formato de erro que o backend devolve (TratadorGlobalDeErros)
export type CampoErro = {
  campo: string
  mensagem: string
}

export type ErroApi = {
  status: number
  codigo: string
  mensagem: string
  campos: CampoErro[]
  timestamp: string
}

// Formato das listagens paginadas do backend (PaginaResponse)
// Atenção: "page" começa em 0 no Spring; a tabela do Ant Design começa em 1
export type Pagina<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// Cliente único da API: todas as telas importam este arquivo
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
})

// Transforma qualquer erro de chamada em uma mensagem pronta para a tela
export function mensagemDeErro(erro: unknown): string {
  if (axios.isAxiosError<ErroApi>(erro)) {
    if (erro.response?.data?.mensagem) {
      return erro.response.data.mensagem
    }
    if (!erro.response) {
      return 'Não foi possível conectar ao servidor. Verifique se o backend está rodando.'
    }
  }
  return 'Ocorreu um erro inesperado. Tente novamente.'
}

// Devolve os erros de validação por campo (status 400), para mostrar no formulário
export function errosDeCampo(erro: unknown): CampoErro[] {
  if (axios.isAxiosError<ErroApi>(erro)) {
    return erro.response?.data?.campos ?? []
  }
  return []
}

export default api
