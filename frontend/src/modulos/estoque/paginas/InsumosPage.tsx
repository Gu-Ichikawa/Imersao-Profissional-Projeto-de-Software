import { useEffect, useState } from 'react'
import { Button, Dropdown, Input, Select, Table, message } from 'antd'
import type { MenuProps } from 'antd'
import { EllipsisOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'
import CabecalhoPagina from '../../../compartilhado/componentes/CabecalhoPagina'
import { mensagemDeErro } from '../../../compartilhado/servicos/api'
import type { Pagina } from '../../../compartilhado/servicos/api'
import { moedaUnitaria, rotuloUnidade } from '../../../compartilhado/formatadores'
import ModalInsumo from '../componentes/ModalInsumo'
import ModalInativarInsumo from '../componentes/ModalInativarInsumo'
import { TagNivelEstoque, TagStatus } from '../componentes/TagsInsumo'
import { ativarInsumo, listarInsumos } from '../servicos/insumoServico'
import type { Insumo } from '../servicos/insumoServico'

type FiltroStatus = 'todos' | 'ativos' | 'inativos'

type Consulta = {
  pagina: number
  tamanho: number
  nome: string
  status: FiltroStatus
}

// Quantidades com separador de milhar: 2000 → "2.000"
function quantidade(valor: number): string {
  return valor.toLocaleString('pt-BR', { maximumFractionDigits: 3 })
}

// Linhas inativas ficam em cinza (padrão do Figma)
function corDoTexto(insumo: Insumo, principal: boolean): string {
  if (!insumo.ativo) return '#9A938B'
  return principal ? '#111111' : '#6B645C'
}

function InsumosPage() {
  const [consulta, setConsulta] = useState<Consulta>({ pagina: 1, tamanho: 20, nome: '', status: 'todos' })
  const [dados, setDados] = useState<Pagina<Insumo> | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  // modal de cadastro/edição: null = fechado; { insumo: null } = novo; { insumo } = editar
  const [modalInsumo, setModalInsumo] = useState<{ insumo: Insumo | null } | null>(null)
  const [paraInativar, setParaInativar] = useState<Insumo | null>(null)
  const [avisos, contextoAvisos] = message.useMessage()

  // Busca a lista sempre que a consulta (página ou filtros) muda
  useEffect(() => {
    let atual = true
    listarInsumos(consulta.pagina, consulta.tamanho, {
      nome: consulta.nome || undefined,
      ativo: consulta.status === 'todos' ? undefined : consulta.status === 'ativos',
    })
      .then((pagina) => {
        if (!atual) return
        setDados(pagina)
        setErro(null)
      })
      .catch((e) => {
        if (atual) setErro(mensagemDeErro(e))
      })
      .finally(() => {
        if (atual) setCarregando(false)
      })
    return () => {
      atual = false
    }
  }, [consulta])

  // Mudar a consulta (mesmo sem mudar nada) dispara uma nova busca
  function atualizar(mudancas: Partial<Consulta> = {}) {
    setCarregando(true)
    setConsulta((c) => ({ ...c, ...mudancas }))
  }

  async function ativar(insumo: Insumo) {
    try {
      await ativarInsumo(insumo.id)
      avisos.success(`“${insumo.nome}” foi reativado.`)
      atualizar()
    } catch (e) {
      avisos.error(mensagemDeErro(e))
    }
  }

  function acoesDoInsumo(insumo: Insumo): MenuProps['items'] {
    return [
      { key: 'editar', label: 'Editar' },
      insumo.ativo ? { key: 'inativar', label: 'Inativar', danger: true } : { key: 'ativar', label: 'Ativar' },
    ]
  }

  function aoEscolherAcao(acao: string, insumo: Insumo) {
    if (acao === 'editar') setModalInsumo({ insumo })
    if (acao === 'inativar') setParaInativar(insumo)
    if (acao === 'ativar') ativar(insumo)
  }

  const filtrando = consulta.nome !== '' || consulta.status !== 'todos'

  return (
    <div>
      {contextoAvisos}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', gap: 24 }}>
        <CabecalhoPagina
          caminho="Estoque / Insumos"
          titulo="Insumos"
          descricao="Cadastre, edite e inative insumos e acompanhe o saldo e o nível de estoque."
        />
        <Button
          type="primary"
          shape="round"
          size="large"
          icon={<PlusOutlined />}
          onClick={() => setModalInsumo({ insumo: null })}
          style={{ marginBottom: 28 }}
        >
          Novo insumo
        </Button>
      </div>

      <div style={{ display: 'flex', gap: 12, marginBottom: 28 }}>
        <Input
          size="large"
          allowClear
          prefix={<SearchOutlined style={{ color: '#9A938B' }} />}
          placeholder="Buscar insumo pelo nome (Enter)"
          style={{ width: 360 }}
          onPressEnter={(e) => atualizar({ nome: e.currentTarget.value.trim(), pagina: 1 })}
          onChange={(e) => {
            if (e.target.value === '' && consulta.nome !== '') atualizar({ nome: '', pagina: 1 })
          }}
        />
        <Select<FiltroStatus>
          size="large"
          style={{ width: 200 }}
          value={consulta.status}
          onChange={(status) => atualizar({ status, pagina: 1 })}
          options={[
            { value: 'todos', label: 'Status: Todos' },
            { value: 'ativos', label: 'Status: Ativos' },
            { value: 'inativos', label: 'Status: Inativos' },
          ]}
        />
      </div>

      {erro && (
        <div role="alert" style={{ background: '#FBEAEA', borderRadius: 10, padding: '12px 16px', color: '#8E2B20', marginBottom: 16 }}>
          {erro}
        </div>
      )}

      <Table<Insumo>
        rowKey="id"
        loading={carregando}
        dataSource={dados?.content ?? []}
        pagination={{
          current: consulta.pagina,
          pageSize: consulta.tamanho,
          total: dados?.totalElements ?? 0,
          showSizeChanger: false,
          hideOnSinglePage: true,
        }}
        onChange={(paginacao) => atualizar({ pagina: paginacao.current ?? 1 })}
        locale={{ emptyText: filtrando ? 'Nenhum insumo encontrado com esses filtros.' : 'Nenhum insumo cadastrado ainda.' }}
        columns={[
          {
            title: 'Insumo',
            render: (_, i) => <span style={{ fontSize: 15, fontWeight: 500, color: corDoTexto(i, true) }}>{i.nome}</span>,
          },
          { title: 'Categoria', render: (_, i) => <span style={{ color: corDoTexto(i, false) }}>{i.categoria.nome}</span> },
          { title: 'Unidade', render: (_, i) => <span style={{ color: corDoTexto(i, false) }}>{rotuloUnidade(i.unidadeMedida)}</span> },
          { title: 'Saldo', render: (_, i) => <span style={{ color: corDoTexto(i, true) }}>{quantidade(i.quantidadeAtual)}</span> },
          { title: 'Mínimo', render: (_, i) => <span style={{ color: corDoTexto(i, true) }}>{quantidade(i.estoqueMinimo)}</span> },
          { title: 'Custo unitário', render: (_, i) => <span style={{ color: corDoTexto(i, true) }}>{moedaUnitaria(i.custoUnitario)}</span> },
          { title: 'Nível', render: (_, i) => <TagNivelEstoque nivel={i.nivelEstoque} /> },
          { title: 'Status', render: (_, i) => <TagStatus ativo={i.ativo} /> },
          {
            title: 'Ações',
            align: 'right',
            render: (_, i) => (
              <Dropdown trigger={['click']} menu={{ items: acoesDoInsumo(i), onClick: ({ key }) => aoEscolherAcao(key, i) }}>
                <Button shape="circle" icon={<EllipsisOutlined />} aria-label={`Ações de ${i.nome}`} />
              </Dropdown>
            ),
          },
        ]}
      />

      {modalInsumo && (
        <ModalInsumo
          insumo={modalInsumo.insumo}
          onFechar={() => setModalInsumo(null)}
          onSalvo={(salvo) => {
            avisos.success(modalInsumo.insumo ? 'Alterações salvas.' : `“${salvo.nome}” foi cadastrado.`)
            setModalInsumo(null)
            atualizar()
          }}
        />
      )}

      {paraInativar && (
        <ModalInativarInsumo
          insumo={paraInativar}
          onFechar={() => setParaInativar(null)}
          onInativado={() => {
            avisos.success(`“${paraInativar.nome}” foi inativado.`)
            setParaInativar(null)
            atualizar()
          }}
        />
      )}
    </div>
  )
}

export default InsumosPage
