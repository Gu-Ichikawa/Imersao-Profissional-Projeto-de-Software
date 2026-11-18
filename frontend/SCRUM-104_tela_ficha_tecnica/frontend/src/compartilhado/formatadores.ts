// Formatadores usados por mais de um módulo

export function moeda(valor: number, casas = 2): string {
  return valor.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    minimumFractionDigits: casas,
    maximumFractionDigits: casas,
  })
}

// Os enums do backend chegam em MAIÚSCULAS: aqui viram texto legível
const ROTULOS_UNIDADE: Record<string, string> = {
  KG: 'kg',
  G: 'g',
  L: 'l',
  ML: 'ml',
  UNIDADE: 'unidade',
}

export function rotuloUnidade(unidade: string): string {
  return ROTULOS_UNIDADE[unidade] ?? unidade.toLowerCase()
}
