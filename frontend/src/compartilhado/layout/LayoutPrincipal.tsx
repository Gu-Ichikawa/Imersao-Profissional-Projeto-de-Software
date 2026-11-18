import { Layout, Menu } from 'antd'
import type { MenuProps } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
  ShopOutlined,
  InboxOutlined,
  TableOutlined,
  DollarOutlined,
  LineChartOutlined,
  CarOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import logo from '../../assets/logo-zentry.svg'

const { Sider, Content } = Layout

const itensMenu: MenuProps['items'] = [
  {
    key: '/cardapio',
    icon: <ShopOutlined />,
    label: 'Cardápio',
    children: [
      { key: '/cardapio/categorias', label: 'Categorias' },
      { key: '/cardapio/pratos', label: 'Pratos' },
    ],
  },
  {
    key: '/estoque',
    icon: <InboxOutlined />,
    label: 'Estoque',
    children: [
      { key: '/estoque/insumos', label: 'Insumos' },
      { key: '/estoque/movimentacoes', label: 'Movimentações' },
    ],
  },
  { key: '/salao', icon: <TableOutlined />, label: 'Salão', disabled: true },
  { key: '/caixa', icon: <DollarOutlined />, label: 'Caixa', disabled: true },
  { key: '/financeiro', icon: <LineChartOutlined />, label: 'Financeiro', disabled: true },
  { key: '/delivery', icon: <CarOutlined />, label: 'Delivery', disabled: true },
  { key: '/usuarios', icon: <TeamOutlined />, label: 'Usuários e Permissões', disabled: true },
]

const rotasDasTelas = [
  '/cardapio/categorias',
  '/cardapio/pratos',
  '/estoque/insumos',
  '/estoque/movimentacoes',
]

function LayoutPrincipal() {
  const navigate = useNavigate()
  const location = useLocation()

  const telaSelecionada =
    rotasDasTelas.find((rota) => location.pathname.startsWith(rota)) ?? ''

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={240} theme="light" style={{ padding: 24 }}>
        <img src={logo} alt="ZENTRY" style={{ width: '100%', marginBottom: 16 }} />
        <Menu
          mode="inline"
          theme="light"
          items={itensMenu}
          defaultOpenKeys={['/cardapio', '/estoque']}
          selectedKeys={[telaSelecionada]}
          onClick={({ key }) => navigate(key)}
          style={{ borderInlineEnd: 'none' }}
        />
           </Sider>
      <Layout>
        <Content style={{ padding: 40 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default LayoutPrincipal