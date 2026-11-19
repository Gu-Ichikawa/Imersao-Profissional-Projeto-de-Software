import { useEffect, useState } from 'react'
import { Button, InputNumber, Modal, Select } from 'antd'
import { mensagemDeErro } from '../../../compartilhado/servicos/api'
import { moeda, rotuloUnidade } from '../../../compartilhado/formatadores'
import { adicionarItem, listarInsumosAtivos } from '../servicos/fichaTecnicaServico'
import type { FichaTecnica, InsumoOpcao } from '../servicos/fichaTecnicaServico'

type ModalAdicionarInsumoProps = {
  aberto: boolean
  pratoId: number
  // insumos que já estão na ficha: não aparecem na lista (o mesmo insumo não entra duas vezes)
  idsNaFicha: number[]
  onFechar: () => void
  onAdicionado: (ficha: FichaTecnica) => void
}

function ModalAdicionarInsumo({ aberto, pratoId, idsNaFicha, onFechar, onAdicionado }: ModalAdicionarInsumoProps) {
  const [insumos, setInsumos] = useState<InsumoOpcao[]>([])
  const [insumoId, setInsumoId] = useState<number | null>(null)
  const [quantidade, setQuantidade] = useState<number | null>(null)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    if (!aberto) return
    listarInsumosAtivos()
      .then(setInsumos)
      .catch((e) => setErro(mensagemDeErro(e)))
  }, [aberto])

  // Ao fechar, limpa o formulário para a próxima vez
  function limpar() {
    setInsumoId(null)
    setQuantidade(null)
    setErro(null)
  }

  function fechar() {
    limpar()
    onFechar()
  }

  const escolhido = insumos.find((i) => i.id === insumoId)
  const custoLinha = escolhido && quantidade ? escolhido.custoUnitario * quantidade : 0
  const podeSalvar = escolhido !== undefined && quantidade !== null && quantidade > 0

  async function salvar() {
    if (!escolhido || !quantidade) return
    setSalvando(true)
    setErro(null)
    try {
      // 409 INSUMO_JA_NA_FICHA e outros erros: mostramos o campo "mensagem" do backend
      const ficha = await adicionarItem(pratoId, escolhido.id, quantidade)
      limpar()
      onAdicionado(ficha)
    } catch (e) {
      setErro(mensagemDeErro(e))
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={aberto}
      onCancel={fechar}
      width={480}
      centered
      destroyOnHidden
      title={<span style={{ fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 30 }}>Adicionar insumo</span>}
      footer={
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
          <Button shape="round" size="large" onClick={fechar} disabled={salvando}>
            Cancelar
          </Button>
          <Button shape="round" size="large" type="primary" onClick={salvar} loading={salvando} disabled={!podeSalvar}>
            Adicionar
          </Button>
        </div>
      }
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16, marginTop: 20 }}>
        {erro && (
          <div
            role="alert"
            style={{ background: '#FBEAEA', borderRadius: 10, padding: '12px 16px', color: '#8E2B20', fontSize: 13 }}
          >
            {erro}
          </div>
        )}

        <div>
          <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 8 }}>Insumo</div>
          <Select
            size="large"
            showSearch
            placeholder="Escolha um insumo"
            optionFilterProp="label"
            style={{ width: '100%' }}
            value={insumoId}
            onChange={setInsumoId}
            options={insumos
              .filter((i) => !idsNaFicha.includes(i.id))
              .map((i) => ({ value: i.id, label: i.nome }))}
          />
        </div>

        <div>
          <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 8 }}>Quantidade</div>
          <div style={{ display: 'flex', gap: 8 }}>
            <InputNumber
              size="large"
              min={0.001}
              placeholder="0"
              value={quantidade}
              onChange={setQuantidade}
              style={{ flex: 1 }}
            />
            <div
              style={{
                width: 70,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: '#FBF8F4',
                border: '1px solid #DDD3C7',
                borderRadius: 10,
                color: '#6B645C',
              }}
            >
              {escolhido ? rotuloUnidade(escolhido.unidadeMedida) : '—'}
            </div>
          </div>
          <div style={{ fontSize: 12, color: '#6B645C', marginTop: 6 }}>A quantidade usa a unidade do insumo.</div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
          <span style={{ color: '#6B645C' }}>Custo desta linha</span>
          <span style={{ fontSize: 24, fontWeight: 500 }}>{moeda(custoLinha)}</span>
        </div>
      </div>
    </Modal>
  )
}

export default ModalAdicionarInsumo
