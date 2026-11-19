import { useEffect, useState } from 'react'
import { Button, Dropdown, Modal, Table, Tag, message } from 'antd'
import type { TableProps } from 'antd'
import { MoreOutlined, PlusOutlined } from '@ant-design/icons'
import axios from 'axios'
import CabecalhoPagina from '../../../compartilhado/componentes/CabecalhoPagina'
import { mensagemDeErro } from '../../../compartilhado/servicos/api'
import type { ErroApi } from '../../../compartilhado/servicos/api'
import ModalCategoria from '../componentes/ModalCategoria'
import {
  ativarCategoria,
  excluirCategoria,
  inativarCategoria,
  listarCategoriasPaginadas,
} from '../servicos/categoriaServico'
import type { Categoria } from '../servicos/categoriaServico'

const TAMANHO_PAGINA = 20

function codigoDeErro(erro: unknown): string | undefined {
  return axios.isAxiosError<ErroApi>(erro) ? erro.response?.data?.codigo : undefined
}

function CategoriasPage() {
  const [mensagem, contextoMensagem] = message.useMessage()
  const [modal, contextoModal] = Modal.useModal()

  const [categorias, setCategorias] = useState<Categoria[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(1)
  const [carregando, setCarregando] = useState(true)

  const [modalAberto, setModalAberto] = useState(false)
  const [categoriaEmEdicao, setCategoriaEmEdicao] = useState<Categoria | null>(null)

  // Incrementar este contador força a tabela a buscar as categorias de novo
  const [recarga, setRecarga] = useState(0)

  useEffect(() => {
    let ativo = true
    listarCategoriasPaginadas(pagina, TAMANHO_PAGINA)
      .then((resultado) => {
        if (!ativo) return
        setCategorias(resultado.content)
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
  }, [pagina, recarga, mensagem])

  function carregarCategorias() {
    setCarregando(true)
    setRecarga((valor) => valor + 1)
  }

  function mudarPagina(novaPagina: number) {
    setCarregando(true)
    setPagina(novaPagina)
  }

  function abrirCadastro() {
    setCategoriaEmEdicao(null)
    setModalAberto(true)
  }

  function abrirEdicao(categoria: Categoria) {
    setCategoriaEmEdicao(categoria)
    setModalAberto(true)
  }

  function aoSalvar(categoria: Categoria) {
    setModalAberto(false)
    mensagem.success(categoriaEmEdicao ? `${categoria.nome} foi atualizada.` : `${categoria.nome} foi cadastrada.`)
    carregarCategorias()
  }

  async function inativar(categoria: Categoria) {
    try {
      await inativarCategoria(categoria.id)
      mensagem.success(`${categoria.nome} foi inativada.`)
      carregarCategorias()
    } catch (erro) {
      mensagem.error(mensagemDeErro(erro))
    }
  }

  function confirmarInativacao(categoria: Categoria) {
    modal.confirm({
      title: 'Inativar categoria?',
      content: `“${categoria.nome}” fica inativa e não recebe pratos novos. Os pratos que já estão nela continuam como estão.`,
      okText: 'Inativar',
      cancelText: 'Cancelar',
      okButtonProps: { shape: 'round' },
      cancelButtonProps: { shape: 'round' },
      onOk: () => inativar(categoria),
    })
  }

  async function reativar(categoria: Categoria) {
    try {
      await ativarCategoria(categoria.id)
      mensagem.success(`${categoria.nome} foi reativada.`)
      carregarCategorias()
    } catch (erro) {
      mensagem.error(mensagemDeErro(erro))
    }
  }

  // UC-CAR-002 FE03: a exclusão foi bloqueada; oferece a inativação no lugar
  function oferecerInativacao(categoria: Categoria, motivo: string) {
    modal.confirm({
      title: 'Não foi possível excluir',
      content: motivo,
      okText: 'Inativar categoria',
      cancelText: 'Agora não',
      okButtonProps: { shape: 'round' },
      cancelButtonProps: { shape: 'round' },
      onOk: () => inativar(categoria),
    })
  }

  function confirmarExclusao(categoria: Categoria) {
    modal.confirm({
      title: 'Excluir categoria?',
      content: `“${categoria.nome}” será excluída definitivamente. Só é possível excluir categorias sem pratos.`,
      okText: 'Excluir',
      cancelText: 'Cancelar',
      okButtonProps: { shape: 'round', danger: true },
      cancelButtonProps: { shape: 'round' },
      onOk: async () => {
        try {
          await excluirCategoria(categoria.id)
          mensagem.success(`${categoria.nome} foi excluída.`)
          carregarCategorias()
        } catch (erro) {
          if (codigoDeErro(erro) === 'CATEGORIA_COM_PRATOS' && categoria.ativa) {
            oferecerInativacao(categoria, mensagemDeErro(erro))
          } else {
            mensagem.error(mensagemDeErro(erro))
          }
        }
      },
    })
  }

  const colunas: TableProps<Categoria>['columns'] = [
    {
      title: 'Categoria',
      dataIndex: 'nome',
      render: (nome: string, categoria) => (
        <span style={{ fontSize: 15, fontWeight: 500, color: categoria.ativa ? undefined : '#9A938B' }}>{nome}</span>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'ativa',
      width: 130,
      render: (ativa: boolean) =>
        ativa ? (
          <Tag bordered={false} style={{ background: '#EDF3EC', color: '#2F5F3A', borderRadius: 999 }}>● Ativa</Tag>
        ) : (
          <Tag bordered={false} style={{ background: '#EFEBE6', color: '#8A837B', borderRadius: 999 }}>● Inativa</Tag>
        ),
    },
    {
      title: 'Ações',
      align: 'right',
      width: 100,
      render: (_, categoria) => (
        <Dropdown
          trigger={['click']}
          menu={{
            items: [
              { key: 'editar', label: 'Editar' },
              categoria.ativa ? { key: 'inativar', label: 'Inativar' } : { key: 'ativar', label: 'Ativar' },
              { key: 'excluir', label: 'Excluir', danger: true },
            ],
            onClick: ({ key }) => {
              if (key === 'editar') abrirEdicao(categoria)
              if (key === 'inativar') confirmarInativacao(categoria)
              if (key === 'ativar') reativar(categoria)
              if (key === 'excluir') confirmarExclusao(categoria)
            },
          }}
        >
          <Button shape="circle" icon={<MoreOutlined />} aria-label={`Mais ações de ${categoria.nome}`} />
        </Dropdown>
      ),
    },
  ]

  return (
    <>
      {contextoMensagem}
      {contextoModal}

      <CabecalhoPagina
        caminho="Cardápio / Categorias"
        titulo="Categorias"
        descricao="Organize os pratos do cardápio em categorias."
        acao={
          <Button type="primary" shape="round" size="large" icon={<PlusOutlined />} onClick={abrirCadastro}>
            Nova categoria
          </Button>
        }
      />

      <Table<Categoria>
        rowKey="id"
        columns={colunas}
        dataSource={categorias}
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
        locale={{ emptyText: 'Nenhuma categoria cadastrada ainda.' }}
      />

      <ModalCategoria
        aberto={modalAberto}
        categoria={categoriaEmEdicao}
        onFechar={() => setModalAberto(false)}
        onSalva={aoSalvar}
      />
    </>
  )
}

export default CategoriasPage
