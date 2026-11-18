import { useParams } from 'react-router-dom'
import CabecalhoPagina from '../../../compartilhado/componentes/CabecalhoPagina'

function FichaTecnicaPage() {
  const { id } = useParams()

  return (
    <CabecalhoPagina
      caminho="Cardápio / Pratos / Ficha técnica"
      titulo="Ficha técnica"
      descricao={`Insumos e custo do prato ${id}.`}
    />
  )
}

export default FichaTecnicaPage
