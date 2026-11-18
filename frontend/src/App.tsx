import { Navigate, Route, Routes } from 'react-router-dom'
import LayoutPrincipal from './compartilhado/layout/LayoutPrincipal'
import CategoriasPage from './modulos/cardapio/paginas/CategoriasPage'
import PratosPage from './modulos/cardapio/paginas/PratosPage'
import FichaTecnicaPage from './modulos/cardapio/paginas/FichaTecnicaPage'
import InsumosPage from './modulos/estoque/paginas/InsumosPage'
import MovimentacoesPage from './modulos/estoque/paginas/MovimentacoesPage'

function App() {
  return (
    <Routes>
      <Route element={<LayoutPrincipal />}>
        <Route index element={<Navigate to="/cardapio/pratos" replace />} />
        <Route path="cardapio/categorias" element={<CategoriasPage />} />
        <Route path="cardapio/pratos" element={<PratosPage />} />
        <Route path="cardapio/pratos/:id/ficha-tecnica" element={<FichaTecnicaPage />} />
        <Route path="estoque/insumos" element={<InsumosPage />} />
        <Route path="estoque/movimentacoes" element={<MovimentacoesPage />} />
        <Route path="*" element={<Navigate to="/cardapio/pratos" replace />} />
      </Route>
    </Routes>
  )
}

export default App
