import type { ReactNode } from 'react'

type CabecalhoPaginaProps = {
  caminho: string
  titulo: string
  descricao?: string
  acao?: ReactNode
}

function CabecalhoPagina({ caminho, titulo, descricao, acao }: CabecalhoPaginaProps) {
  return (
    <div style={{ display: 'flex', alignItems: 'flex-end', gap: 24, marginBottom: 28 }}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6, flex: 1 }}>
        <span style={{ fontSize: 13, color: '#6B645C' }}>{caminho}</span>
        <h1 style={{ margin: 0, fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 44, color: '#111111' }}>
          {titulo}
        </h1>
        {descricao && <span style={{ fontSize: 15, color: '#6B645C' }}>{descricao}</span>}
      </div>
      {acao}
    </div>
  )
}

export default CabecalhoPagina
