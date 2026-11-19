// Formatadores usados por mais de um módulo

export function moeda(valor: number, casas = 2): string {
  return valor.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    minimumFractionDigits: casas,
    maximumFractionDigits: casas,
  })
}

export function moedaUnitaria(valor: number): string {
  const centavosExatos = Number.isInteger(Math.round(valor * 10000) / 100)
  return moeda(valor, centavosExatos ? 2 : 4)
}

// Os enums do backend chegam em MAIÚSCULAS: aqui viram texto legível
const ROTULOS_UNIDADE: Record<string, string> = {
  KG: 'kg',
  G: 'g',
  L: 'L',
  ML: 'ml',
  UNIDADE: 'un',
}

export function rotuloUnidade(unidade: string): string {
  return ROTULOS_UNIDADE[unidade] ?? unidade.toLowerCase()
}
