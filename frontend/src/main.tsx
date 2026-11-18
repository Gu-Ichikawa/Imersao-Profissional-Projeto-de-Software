import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import type { ThemeConfig } from 'antd'
import ptBR from 'antd/locale/pt_BR'
import App from './App.tsx'

const tema: ThemeConfig = {
  token: {
    colorPrimary: '#111111',
    colorText: '#111111',
    colorTextSecondary: '#6B645C',
    colorBorder: '#DDD3C7',
    borderRadius: 10,
    fontFamily: "'Inter', sans-serif",
  },
  components: {
    Layout: {
      siderBg: '#F3ECE4',
      bodyBg: '#FBF8F4',
    },
    Menu: {
      itemBg: '#F3ECE4',
      subMenuItemBg: '#F3ECE4',
      itemSelectedBg: '#111111',
      itemSelectedColor: '#F3ECE4',
      itemBorderRadius: 10,
    },
  },
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ConfigProvider locale={ptBR} theme={tema}>
        <App />
      </ConfigProvider>
    </BrowserRouter>
  </StrictMode>,
)