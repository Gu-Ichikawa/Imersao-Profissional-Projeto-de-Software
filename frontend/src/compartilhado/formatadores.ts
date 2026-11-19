// Formatadores usados por mais de um módulo

// Dinheiro: "R$ 32,00"
export function moeda(valor: number, casas = 2): string {
  return valor.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    minimumFractionDigits: casas,
    maximumFractionDigits: casas,
  })
}

// Custo unitário de insumo: 2 casas quando dá centavos exatos ("R$ 1,20"),
// senão 4 casas ("R$ 0,0380"), como no Figma
export function moedaUnitaria(valor: number): string {
  const centavosExatos = Number.isInteger(Math.round(valor * 10000) / 100)
  return moeda(valor, centavosExatos ? 2 : 4)
}

// Os enums do backend chegam em MAIÚSCULAS: aqui viram o texto do Figma
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
