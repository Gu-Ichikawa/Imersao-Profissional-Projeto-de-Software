type CabecalhoPaginaProps = {
  caminho: string
  titulo: string
  descricao?: string
}

function CabecalhoPagina({ caminho, titulo, descricao }: CabecalhoPaginaProps) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 28 }}>
      <span style={{ fontSize: 13, color: '#6B645C' }}>{caminho}</span>
      <h1 style={{ margin: 0, fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 44, color: '#111111' }}>
        {titulo}
      </h1>
      {descricao && <span style={{ fontSize: 15, color: '#6B645C' }}>{descricao}</span>}
    </div>
  )
}

export default CabecalhoPagina
