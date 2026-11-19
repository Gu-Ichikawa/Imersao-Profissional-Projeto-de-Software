import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button, InputNumber, Popconfirm, Spin, Table } from 'antd'
import { ArrowLeftOutlined, PlusOutlined } from '@ant-design/icons'
import CabecalhoPagina from '../../../compartilhado/componentes/CabecalhoPagina'
import { mensagemDeErro } from '../../../compartilhado/servicos/api'
import { moeda, moedaUnitaria, rotuloUnidade } from '../../../compartilhado/formatadores'
import ModalAdicionarInsumo from '../componentes/ModalAdicionarInsumo'
import { alterarQuantidade, buscarFicha, removerItem } from '../servicos/fichaTecnicaServico'
import type { FichaTecnica, ItemFicha } from '../servicos/fichaTecnicaServico'

function FichaTecnicaPage() {
  const { id } = useParams()
  const pratoId = Number(id)
  const navigate = useNavigate()

  const [ficha, setFicha] = useState<FichaTecnica | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [modalAberto, setModalAberto] = useState(false)

  // Busca inicial da ficha ao abrir a tela
  useEffect(() => {
    buscarFicha(pratoId)
      .then(setFicha)
      .catch((e) => setErro(mensagemDeErro(e)))
      .finally(() => setCarregando(false))
  }, [pratoId])

  // Se uma alteração falhar, recarrega a ficha para mostrar o que está salvo
  async function recarregar() {
    try {
      setFicha(await buscarFicha(pratoId))
    } catch {
      // mantém a mensagem de erro que já está na tela
    }
  }

  // A API devolve a ficha inteira já com o custo recalculado
  async function aplicar(acao: Promise<FichaTecnica>) {
    setErro(null)
    try {
      setFicha(await acao)
    } catch (e) {
      setErro(mensagemDeErro(e))
      await recarregar()
    }
  }

  function aoAlterarQuantidade(item: ItemFicha, nova: number | null) {
    if (nova === null || nova <= 0 || nova === item.quantidade) {
      setFicha((f) => (f ? { ...f } : f)) // volta o campo para o valor salvo
      return
    }
    aplicar(alterarQuantidade(pratoId, item.id, nova))
  }

  if (carregando && !ficha) {
    return <Spin style={{ display: 'block', marginTop: 80 }} />
  }

  return (
    <div>
      <Button type="text" icon={<ArrowLeftOutlined />} onClick={() => navigate('/cardapio/pratos')} style={{ marginBottom: 16, paddingLeft: 0 }}>
        Voltar para pratos
      </Button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 24 }}>
        <CabecalhoPagina
          caminho="Cardápio / Pratos / Ficha técnica"
          titulo="Ficha técnica"
          descricao={ficha ? `${ficha.pratoNome} · ${ficha.categoriaNome}` : undefined}
        />
        {ficha && (
          <div style={{ background: '#111111', color: '#F3ECE4', borderRadius: 16, padding: '16px 28px', minWidth: 200 }}>
            <div style={{ fontSize: 13 }}>Custo do prato</div>
            <div style={{ fontFamily: "'Jost', sans-serif", fontSize: 40, lineHeight: 1.2 }}>{moeda(ficha.custoTotal)}</div>
            <div style={{ fontSize: 12 }}>Preço de venda · {moeda(ficha.precoVenda)}</div>
          </div>
        )}
      </div>

      {erro && (
        <div role="alert" style={{ background: '#FBEAEA', borderRadius: 10, padding: '12px 16px', color: '#8E2B20', marginBottom: 16 }}>
          {erro}
        </div>
      )}

      {ficha && (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h2 style={{ margin: 0, fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 28 }}>Insumos do prato</h2>
            <Button type="primary" shape="round" size="large" icon={<PlusOutlined />} onClick={() => setModalAberto(true)}>
              Adicionar insumo
            </Button>
          </div>

          <Table<ItemFicha>
            rowKey="id"
            pagination={false}
            dataSource={ficha.itens}
            locale={{ emptyText: 'Esta ficha ainda não tem insumos. Clique em "Adicionar insumo".' }}
            columns={[
              { title: 'Insumo', dataIndex: 'insumoNome' },
              {
                title: 'Quantidade',
                width: 190,
                render: (_, item) => (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <InputNumber
                      key={`${item.id}-${item.quantidade}`}
                      min={0.001}
                      defaultValue={item.quantidade}
                      style={{ width: 90 }}
                      onBlur={(e) => aoAlterarQuantidade(item, e.target.value === '' ? null : Number(e.target.value.replace(',', '.')))}
                      onPressEnter={(e) => e.currentTarget.blur()}
                    />
                    <span style={{ color: '#6B645C' }}>{rotuloUnidade(item.unidadeMedida)}</span>
                  </div>
                ),
              },
              {
                title: 'Custo unitário',
                render: (_, item) => (
                  <span style={{ color: '#6B645C' }}>
                    {moedaUnitaria(item.custoUnitario)} / {rotuloUnidade(item.unidadeMedida)}
                  </span>
                ),
              },
              { title: 'Custo', render: (_, item) => <strong>{moeda(item.custo)}</strong> },
              {
                title: 'Ações',
                align: 'right',
                render: (_, item) => (
                  <Popconfirm
                    title="Remover este insumo da ficha?"
                    okText="Remover"
                    cancelText="Cancelar"
                    onConfirm={() => aplicar(removerItem(pratoId, item.id))}
                  >
                    <Button type="link" danger>
                      Remover
                    </Button>
                  </Popconfirm>
                ),
              },
            ]}
            summary={() => (
              <Table.Summary.Row>
                <Table.Summary.Cell index={0} colSpan={3}>
                  <strong>Custo total do prato</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={3} colSpan={2} align="right">
                  <span style={{ fontSize: 20, fontWeight: 500 }}>{moeda(ficha.custoTotal)}</span>
                </Table.Summary.Cell>
              </Table.Summary.Row>
            )}
          />
          <p style={{ color: '#6B645C', fontSize: 13, marginTop: 16 }}>O custo é recalculado a cada alteração na ficha.</p>

          <ModalAdicionarInsumo
            aberto={modalAberto}
            pratoId={pratoId}
            idsNaFicha={ficha.itens.map((i) => i.insumoId)}
            onFechar={() => setModalAberto(false)}
            onAdicionado={(nova) => {
              setFicha(nova)
              setModalAberto(false)
            }}
          />
        </>
      )}
    </div>
  )
}

export default FichaTecnicaPage
