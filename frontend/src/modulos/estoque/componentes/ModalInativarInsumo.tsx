import { useState } from 'react'
import { Button, Modal } from 'antd'
import { mensagemDeErro } from '../../../compartilhado/servicos/api'
import { inativarInsumo } from '../servicos/insumoServico'
import type { Insumo } from '../servicos/insumoServico'

type ModalInativarInsumoProps = {
  insumo: Insumo
  onFechar: () => void
  onInativado: () => void
}

function ModalInativarInsumo({ insumo, onFechar, onInativado }: ModalInativarInsumoProps) {
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function confirmar() {
    setSalvando(true)
    setErro(null)
    try {
      await inativarInsumo(insumo.id)
      onInativado()
    } catch (e) {
      setErro(mensagemDeErro(e))
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open
      centered
      width={480}
      onCancel={onFechar}
      maskClosable={!salvando}
      title={<span style={{ fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 30 }}>Inativar insumo?</span>}
      footer={
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
          <Button shape="round" size="large" onClick={onFechar} disabled={salvando}>
            Cancelar
          </Button>
          <Button shape="round" size="large" type="primary" onClick={confirmar} loading={salvando}>
            Inativar
          </Button>
        </div>
      }
    >
      {erro && (
        <div role="alert" style={{ background: '#FBEAEA', borderRadius: 10, padding: '12px 16px', color: '#8E2B20', margin: '16px 0' }}>
          {erro}
        </div>
      )}
      <p style={{ color: '#6B645C', fontSize: 15, marginTop: 16 }}>
        “{insumo.nome}” deixa de aparecer na lista de insumos ativos, mas não é apagado: o saldo e o histórico de
        movimentações são mantidos e você pode reativá-lo quando quiser.
      </p>
    </Modal>
  )
}

export default ModalInativarInsumo
