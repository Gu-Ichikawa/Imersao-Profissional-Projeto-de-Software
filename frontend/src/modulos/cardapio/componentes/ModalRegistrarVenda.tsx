import { useState } from 'react'
import { Button, InputNumber, Modal, notification } from 'antd'
import axios from 'axios'
import { mensagemDeErro } from '../../../compartilhado/servicos/api'
import type { ErroApi } from '../../../compartilhado/servicos/api'
import { moeda } from '../../../compartilhado/formatadores'
import { registrarVenda } from '../servicos/vendaServico'

// Só o que o modal precisa saber do prato (a PratosPage passa o prato da linha)
export type PratoParaVenda = {
  id: number
  nome: string
  precoVenda: number
}

type ModalRegistrarVendaProps = {
  prato: PratoParaVenda | null
  onFechar: () => void
  onVendaRegistrada?: () => void
}

function codigoDeErro(erro: unknown): string | undefined {
  return axios.isAxiosError<ErroApi>(erro) ? erro.response?.data?.codigo : undefined
}

function ModalRegistrarVenda({ prato, onFechar, onVendaRegistrada }: ModalRegistrarVendaProps) {
  const [quantidade, setQuantidade] = useState<number>(1)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState<{ titulo: string; mensagem: string } | null>(null)

  // Ao fechar, volta ao estado inicial para a próxima venda
  function fechar() {
    setQuantidade(1)
    setErro(null)
    onFechar()
  }

  async function confirmar() {
    if (!prato) return
    setSalvando(true)
    setErro(null)
    try {
      await registrarVenda(prato.id, quantidade)
      notification.success({
        message: 'Venda registrada',
        description: `${quantidade}x ${prato.nome} · ${moeda(prato.precoVenda * quantidade)}. O estoque foi atualizado.`,
        placement: 'topRight',
      })
      onVendaRegistrada?.()
      fechar()
    } catch (e) {
      setErro({
        titulo: codigoDeErro(e) === 'ESTOQUE_INSUFICIENTE' ? 'Estoque insuficiente' : 'Não foi possível registrar a venda',
        mensagem: mensagemDeErro(e),
      })
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={prato !== null}
      onCancel={fechar}
      width={480}
      centered
      destroyOnHidden
      title={<span style={{ fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 30 }}>Registrar venda</span>}
      footer={
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
          <Button shape="round" size="large" onClick={fechar} disabled={salvando}>
            Cancelar
          </Button>
          <Button shape="round" size="large" type="primary" onClick={confirmar} loading={salvando}>
            Confirmar venda
          </Button>
        </div>
      }
    >
      {prato && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16, marginTop: 20 }}>
          {erro && (
            <div role="alert" style={{ background: '#FBEAEA', borderRadius: 10, padding: '12px 16px', color: '#8E2B20' }}>
              <div style={{ fontWeight: 600 }}>{erro.titulo}</div>
              <div style={{ fontSize: 13 }}>{erro.mensagem}</div>
            </div>
          )}

          <div style={{ background: '#FBF8F4', border: '1px solid #DDD3C7', borderRadius: 10, padding: '12px 16px' }}>
            <div style={{ fontWeight: 500, fontSize: 16 }}>{prato.nome}</div>
            <div style={{ fontSize: 13, color: '#6B645C' }}>Preço unitário · {moeda(prato.precoVenda)}</div>
          </div>

          <div>
            <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 8 }}>Quantidade vendida</div>
            <div style={{ display: 'flex', gap: 8 }}>
              <Button
                shape="round"
                size="large"
                aria-label="Diminuir quantidade"
                disabled={quantidade <= 1 || salvando}
                onClick={() => setQuantidade((q) => Math.max(1, q - 1))}
              >
                –
              </Button>
              <InputNumber
                size="large"
                min={1}
                max={999}
                precision={0}
                controls={false}
                value={quantidade}
                status={erro ? 'error' : undefined}
                onChange={(v) => setQuantidade(v ?? 1)}
                style={{ width: 94, textAlign: 'center' }}
              />
              <Button
                shape="round"
                size="large"
                aria-label="Aumentar quantidade"
                disabled={quantidade >= 999 || salvando}
                onClick={() => setQuantidade((q) => Math.min(999, q + 1))}
              >
                +
              </Button>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <span style={{ color: '#6B645C' }}>Total</span>
            <span style={{ fontSize: 24, fontWeight: 500 }}>{moeda(prato.precoVenda * quantidade)}</span>
          </div>
        </div>
      )}
    </Modal>
  )
}

export default ModalRegistrarVenda
