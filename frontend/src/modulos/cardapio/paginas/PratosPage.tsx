import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Dropdown, Input, Modal, Select, Table, Tag, Tooltip, message } from 'antd'
import type { TableProps } from 'antd'
import { MoreOutlined, PlusOutlined } from '@ant-design/icons'
import CabecalhoPagina from '../../../compartilhado/componentes/CabecalhoPagina'
import { mensagemDeErro } from '../../../compartilhado/servicos/api'
import ModalPrato from '../componentes/ModalPrato'
import ModalRegistrarVenda from '../componentes/ModalRegistrarVenda'
import type { PratoParaVenda } from '../componentes/ModalRegistrarVenda'
import { ativarPrato, inativarPrato, listarCategorias, listarPratos } from '../servicos/pratoServico'
import type { CategoriaCardapio, FiltrosPrato, Prato } from '../servicos/pratoServico'

const TAMANHO_PAGINA = 20

const formatoMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

function PratosPage() {
  const navigate = useNavigate()
  const [mensagem, contextoMensagem] = message.useMessage()
  const [modal, contextoModal] = Modal.useModal()

  const [pratos, setPratos] = useState<Prato[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(1)
  const [carregando, setCarregando] = useState(true)
  const [filtros, setFiltros] = useState<FiltrosPrato>({})
  const [categorias, setCategorias] = useState<CategoriaCardapio[]>([])

  const [modalAberto, setModalAberto] = useState(false)
  const [pratoEmEdicao, setPratoEmEdicao] = useState<Prato | null>(null)
  const [pratoVenda, setPratoVenda] = useState<PratoParaVenda | null>(null)

  // Incrementar este contador força a tabela a buscar os pratos de novo
  const [recarga, setRecarga] = useState(0)

  useEffect(() => {
    let ativo = true
    listarPratos(filtros, pagina, TAMANHO_PAGINA)
      .then((resultado) => {
        if (!ativo) return
        setPratos(resultado.content)
        setTotal(resultado.totalElements)
      })
      .catch((erro) => {
        if (ativo) mensagem.error(mensagemDeErro(erro))
      })
      .finally(() => {
        if (ativo) setCarregando(false)
      })
    return () => {
      ativo = false
    }
  }, [filtros, pagina, recarga, mensagem])

  function carregarPratos() {
    setCarregando(true)
    setRecarga((valor) => valor + 1)
  }

  function mudarPagina(novaPagina: number) {
    setCarregando(true)
    setPagina(novaPagina)
  }

  useEffect(() => {
    listarCategorias()
      .then(setCategorias)
      .catch((erro) => mensagem.error(mensagemDeErro(erro)))
  }, [mensagem])

  function mudarFiltro(novos: FiltrosPrato) {
    setCarregando(true)
    setFiltros((atuais) => ({ ...atuais, ...novos }))
    setPagina(1)
  }

  function abrirCadastro() {
    setPratoEmEdicao(null)
    setModalAberto(true)
  }

  function abrirEdicao(prato: Prato) {
    setPratoEmEdicao(prato)
    setModalAberto(true)
  }

  function aoSalvar(prato: Prato) {
    setModalAberto(false)
    mensagem.success(pratoEmEdicao ? `${prato.nome} foi atualizado.` : `${prato.nome} foi cadastrado.`)
    carregarPratos()
  }

  function confirmarInativacao(prato: Prato) {
    modal.confirm({
      title: 'Inativar prato?',
      content: `“${prato.nome}” deixa de aparecer no cardápio e não poderá ser vendido. O histórico do prato é mantido e você pode reativá-lo quando quiser.`,
      okText: 'Inativar',
      cancelText: 'Cancelar',
      okButtonProps: { shape: 'round' },
      cancelButtonProps: { shape: 'round' },
      onOk: async () => {
        try {
          await inativarPrato(prato.id)
          mensagem.success(`${prato.nome} foi inativado.`)
          carregarPratos()
        } catch (erro) {
          mensagem.error(mensagemDeErro(erro))
        }
      },
    })
  }

  async function reativar(prato: Prato) {
    try {
      await ativarPrato(prato.id)
      mensagem.success(`${prato.nome} foi reativado.`)
      carregarPratos()
    } catch (erro) {
      mensagem.error(mensagemDeErro(erro))
    }
  }

  const corInativo = (prato: Prato) => (prato.ativo ? undefined : '#9A938B')

  const colunas: TableProps<Prato>['columns'] = [
    {
      title: 'Prato',
      dataIndex: 'nome',
      render: (nome: string, prato) => (
        <span style={{ fontSize: 15, fontWeight: 500, color: corInativo(prato) }}>{nome}</span>
      ),
    },
    {
      title: 'Categoria',
      width: 160,
      render: (_, prato) => <span style={{ color: '#6B645C' }}>{prato.categoria.nome}</span>,
    },
    {
      title: 'Preço',
      dataIndex: 'precoVenda',
      width: 120,
      render: (preco: number, prato) => <span style={{ color: corInativo(prato) }}>{formatoMoeda.format(preco)}</span>,
    },
    {
      title: 'Status',
      dataIndex: 'ativo',
      width: 110,
      render: (ativo: boolean) =>
        ativo ? (
          <Tag bordered={false} style={{ background: '#EDF3EC', color: '#2F5F3A', borderRadius: 999 }}>● Ativo</Tag>
        ) : (
          <Tag bordered={false} style={{ background: '#EFEBE6', color: '#8A837B', borderRadius: 999 }}>● Inativo</Tag>
        ),
    },
    {
      title: 'Ações',
      align: 'right',
      width: 340,
      render: (_, prato) => (
        <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
          <Button shape="round" onClick={() => navigate(`/cardapio/pratos/${prato.id}/ficha-tecnica`)}>
            Ficha técnica
          </Button>
          <Tooltip title={prato.ativo ? '' : 'Prato inativo não pode ser vendido'}>
            <Button
              shape="round"
              type="primary"
              disabled={!prato.ativo}
              onClick={() => setPratoVenda({ id: prato.id, nome: prato.nome, precoVenda: prato.precoVenda })}
            >
              Vender
            </Button>
          </Tooltip>
          <Dropdown
            trigger={['click']}
            menu={{
              items: [
                { key: 'editar', label: 'Editar' },
                prato.ativo
                  ? { key: 'inativar', label: 'Inativar', danger: true }
                  : { key: 'ativar', label: 'Ativar' },
              ],
              onClick: ({ key }) => {
                if (key === 'editar') abrirEdicao(prato)
                if (key === 'inativar') confirmarInativacao(prato)
                if (key === 'ativar') reativar(prato)
              },
            }}
          >
            <Button shape="circle" icon={<MoreOutlined />} aria-label={`Mais ações de ${prato.nome}`} />
          </Dropdown>
        </div>
      ),
    },
  ]

  return (
    <>
      {contextoMensagem}
      {contextoModal}

      <CabecalhoPagina
        caminho="Cardápio / Pratos"
        titulo="Pratos"
        descricao="Cadastre, edite e inative pratos, monte a ficha técnica ou registre uma venda."
        acao={
          <Button type="primary" shape="round" size="large" icon={<PlusOutlined />} onClick={abrirCadastro}>
            Novo prato
          </Button>
        }
      />

      <div style={{ display: 'flex', gap: 12, marginBottom: 28 }}>
        <Input.Search
          placeholder="Buscar prato pelo nome"
          allowClear
          size="large"
          style={{ width: 360 }}
          onSearch={(valor) => mudarFiltro({ nome: valor || undefined })}
        />
        <Select
          placeholder="Categoria: Todas"
          allowClear
          size="large"
          style={{ width: 220 }}
          options={categorias.map((c) => ({ value: c.id, label: c.nome }))}
          onChange={(valor?: number) => mudarFiltro({ categoriaId: valor })}
        />
        <Select
          placeholder="Status: Todos"
          allowClear
          size="large"
          style={{ width: 200 }}
          options={[
            { value: true, label: 'Ativos' },
            { value: false, label: 'Inativos' },
          ]}
          onChange={(valor?: boolean) => mudarFiltro({ ativo: valor })}
        />
      </div>

      <Table<Prato>
        rowKey="id"
        columns={colunas}
        dataSource={pratos}
        loading={carregando}
        style={{ background: '#FFFFFF', border: '1px solid #DDD3C7', borderRadius: 16, overflow: 'hidden' }}
        pagination={{
          current: pagina,
          pageSize: TAMANHO_PAGINA,
          total,
          showSizeChanger: false,
          hideOnSinglePage: true,
          onChange: mudarPagina,
        }}
        locale={{ emptyText: 'Nenhum prato encontrado.' }}
      />

      <ModalPrato
        aberto={modalAberto}
        prato={pratoEmEdicao}
        categorias={categorias}
        onFechar={() => setModalAberto(false)}
        onSalvo={aoSalvar}
      />

      <ModalRegistrarVenda prato={pratoVenda} onFechar={() => setPratoVenda(null)} />
    </>
  )
}

export default PratosPage
