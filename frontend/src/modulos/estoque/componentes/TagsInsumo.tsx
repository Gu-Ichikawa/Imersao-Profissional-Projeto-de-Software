import type { NivelEstoque } from '../servicos/insumoServico'

const NIVEIS: Record<NivelEstoque, { rotulo: string; fundo: string; texto: string }> = {
  SUFICIENTE: { rotulo: 'Suficiente', fundo: '#EDF3EC', texto: '#2F5F3A' },
  ATENCAO: { rotulo: 'Atenção', fundo: '#FBF1DF', texto: '#8A5A00' },
  REPOR: { rotulo: 'Repor', fundo: '#FBEAEA', texto: '#8E2B20' },
}

type PilulaProps = {
  rotulo: string
  fundo: string
  texto: string
}

function Pilula({ rotulo, fundo, texto }: PilulaProps) {
  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 6,
        padding: '4px 10px',
        borderRadius: 999,
        background: fundo,
        color: texto,
        fontSize: 12,
        fontWeight: 500,
        whiteSpace: 'nowrap',
      }}
    >
      <span style={{ width: 6, height: 6, borderRadius: '50%', background: texto }} />
      {rotulo}
    </span>
  )
}

export function TagNivelEstoque({ nivel }: { nivel: NivelEstoque | null }) {
  if (!nivel) {
    return <span style={{ color: '#9A938B' }}>—</span>
  }
  return <Pilula {...NIVEIS[nivel]} />
}

export function TagStatus({ ativo }: { ativo: boolean }) {
  return ativo ? (
    <Pilula rotulo="Ativo" fundo="#EDF3EC" texto="#2F5F3A" />
  ) : (
    <Pilula rotulo="Inativo" fundo="#EFEBE6" texto="#8A837B" />
  )
}
