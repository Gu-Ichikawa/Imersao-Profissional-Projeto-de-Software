import { useEffect, useState } from 'react'
import { Button, Select, Table, message } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import CabecalhoPagina from '../../../compartilhado/componentes/CabecalhoPagina'
import { mensagemDeErro } from '../../../compartilhado/servicos/api'
import type { Pagina } from '../../../compartilhado/servicos/api'
import { rotuloUnidade } from '../../../compartilhado/formatadores'
import ModalMovimentacao from '../componentes/ModalMovimentacao'
import { listarInsumos } from '../servicos/insumoServico'
import type { Insumo } from '../servicos/insumoServico'
import { ROTULOS_MOTIVO, ROTULOS_TIPO, listarHistorico } from '../servicos/movimentacaoServico'
import type { Movimentacao } from '../servicos/movimentacaoServico'

type Consulta = {
  insumoId: number | null
  pagina: number
  tamanho: number
}

function quantidade(valor: number): string {
  return valor.toLocaleString('pt-BR', { maximumFractionDigits: 3 })
}

// A API manda "2026-11-19T15:30:00.123456": os microssegundos são cortados antes de converter
function dataHora(valor: string): string {
  return new Date(valor.slice(0, 19)).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

function MovimentacoesPage() {
  const [consulta, setConsulta] = useState<Consulta>({ insumoId: null, pagina: 1, tamanho: 20 })
  const [insumos, setInsumos] = useState<Insumo[]>([])
  const [dados, setDados] = useState<Pagina<Movimentacao> | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [modalAberto, setModalAberto] = useState(false)
  const [avisos, contextoAvisos] = message.useMessage()

  // Os insumos são recarregados junto com o histórico, porque toda movimentação muda o saldo.
  // Ativos e inativos, até 50 (o máximo por página da API)
  useEffect(() => {
    let atual = true
    Promise.all([
      listarInsumos(1, 50, {}),
      consulta.insumoId ? listarHistorico(consulta.insumoId, consulta.pagina, consulta.tamanho) : Promise.resolve(null),
    ])
      .then(([paginaInsumos, historico]) => {
        if (!atual) return
        setInsumos(paginaInsumos.content)
        setDados(historico)
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

  function atualizar(mudancas: Partial<Consulta> = {}) {
    setCarregando(true)
    setConsulta((c) => ({ ...c, ...mudancas }))
  }

  const escolhido = insumos.find((i) => i.id === consulta.insumoId)

  return (
    <div>
      {contextoAvisos}

      <CabecalhoPagina
        caminho="Estoque / Movimentações"
        titulo="Movimentações"
        descricao="Registre entradas, retiradas e ajustes e consulte o histórico."
        acao={
          <Button type="primary" shape="round" size="large" icon={<PlusOutlined />} onClick={() => setModalAberto(true)}>
            Nova movimentação
          </Button>
        }
      />

      <div style={{ display: 'flex', alignItems: 'center', gap: 24, marginBottom: 28 }}>
        <Select
          size="large"
          showSearch
          optionFilterProp="label"
          placeholder="Escolha um insumo para ver o histórico"
          style={{ width: 360 }}
          value={consulta.insumoId ?? undefined}
          onChange={(insumoId) => atualizar({ insumoId, pagina: 1 })}
          options={insumos.map((i) => ({ value: i.id, label: i.ativo ? i.nome : `${i.nome} (inativo)` }))}
        />
        {escolhido && (
          <span style={{ fontSize: 15, color: '#6B645C' }}>
            Saldo atual:{' '}
            <strong style={{ color: '#111111' }}>
              {quantidade(escolhido.quantidadeAtual)} {rotuloUnidade(escolhido.unidadeMedida)}
            </strong>
          </span>
        )}
      </div>

      {erro && (
        <div role="alert" style={{ background: '#FBEAEA', borderRadius: 10, padding: '12px 16px', color: '#8E2B20', marginBottom: 16 }}>
          {erro}
        </div>
      )}

      <Table<Movimentacao>
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
        locale={{
          emptyText: consulta.insumoId
            ? 'Nenhuma movimentação registrada para este insumo.'
            : 'Escolha um insumo para ver o histórico.',
        }}
        columns={[
          { title: 'Data/hora', render: (_, m) => <span style={{ color: '#6B645C' }}>{dataHora(m.dataHora)}</span> },
          { title: 'Tipo', render: (_, m) => <span style={{ fontSize: 15, fontWeight: 500 }}>{ROTULOS_TIPO[m.tipo]}</span> },
          {
            title: 'Quantidade',
            render: (_, m) => (
              <span>
                {quantidade(m.quantidade)} {escolhido && rotuloUnidade(escolhido.unidadeMedida)}
              </span>
            ),
          },
          {
            title: 'Motivo',
            render: (_, m) => (m.motivo ? ROTULOS_MOTIVO[m.motivo] : <span style={{ color: '#9A938B' }}>—</span>),
          },
        ]}
      />

      {modalAberto && (
        <ModalMovimentacao
          insumos={insumos}
          insumoId={consulta.insumoId}
          onFechar={() => setModalAberto(false)}
          onSalvo={(insumoId) => {
            avisos.success('Movimentação registrada.')
            setModalAberto(false)
            atualizar({ insumoId, pagina: 1 })
          }}
        />
      )}
    </div>
  )
}

export default MovimentacoesPage
