import { useState } from 'react'
import { Button, Form, InputNumber, Modal, Select } from 'antd'
import { errosDeCampo, mensagemDeErro } from '../../../compartilhado/servicos/api'
import { rotuloUnidade } from '../../../compartilhado/formatadores'
import type { Insumo } from '../servicos/insumoServico'
import { MOTIVOS, ROTULOS_MOTIVO, ROTULOS_TIPO, ajustarEstoque, registrarMovimentacao } from '../servicos/movimentacaoServico'
import type { MotivoAjuste } from '../servicos/movimentacaoServico'

// BAIXA_POR_VENDA não aparece no formulário: só a venda gera esse tipo
type TipoFormulario = 'ENTRADA' | 'RETIRADA' | 'AJUSTE'

type ValoresFormulario = {
  insumoId: number
  tipo: TipoFormulario
  quantidade: number
  motivo?: MotivoAjuste
}

type ModalMovimentacaoProps = {
  insumos: Insumo[]
  // insumo escolhido na tela (null = nenhum)
  insumoId: number | null
  onFechar: () => void
  onSalvo: (insumoId: number) => void
}

function quantidade(valor: number, sinal = false): string {
  return valor.toLocaleString('pt-BR', { maximumFractionDigits: 3, signDisplay: sinal ? 'exceptZero' : 'auto' })
}

function ModalMovimentacao({ insumos, insumoId, onFechar, onSalvo }: ModalMovimentacaoProps) {
  const [form] = Form.useForm<ValoresFormulario>()
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  const tipo = Form.useWatch('tipo', form)
  const idEscolhido = Form.useWatch('insumoId', form)
  const quantidadeInformada = Form.useWatch('quantidade', form)

  const ajuste = tipo === 'AJUSTE'
  const escolhido = insumos.find((i) => i.id === idEscolhido)
  const unidade = escolhido ? rotuloUnidade(escolhido.unidadeMedida) : undefined
  // No ajuste, mostra a diferença antes de confirmar (UC-EST-003, passos 4 e 5)
  const diferenca =
    ajuste && escolhido && quantidadeInformada != null ? quantidadeInformada - escolhido.quantidadeAtual : null

  async function salvar() {
    let valores: ValoresFormulario
    try {
      valores = await form.validateFields()
    } catch {
      return
    }

    setSalvando(true)
    setErro(null)
    try {
      if (valores.tipo === 'AJUSTE') {
        await ajustarEstoque({ insumoId: valores.insumoId, quantidadeContada: valores.quantidade, motivo: valores.motivo! })
      } else {
        await registrarMovimentacao({ insumoId: valores.insumoId, tipo: valores.tipo, quantidade: valores.quantidade })
      }
      onSalvo(valores.insumoId)
    } catch (e) {
      const campos = errosDeCampo(e)
      if (campos.length > 0) {
        // No ajuste a API chama a quantidade de "quantidadeContada"; no formulário o campo é um só
        form.setFields(
          campos.map((c) => ({
            name: (c.campo === 'quantidadeContada' ? 'quantidade' : c.campo) as keyof ValoresFormulario,
            errors: [c.mensagem],
          })),
        )
      }
      // ESTOQUE_INSUFICIENTE e outros erros: mostramos o campo "mensagem" do backend
      setErro(mensagemDeErro(e))
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open
      centered
      width={520}
      onCancel={onFechar}
      maskClosable={!salvando}
      title={<span style={{ fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 30 }}>Nova movimentação</span>}
      footer={
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
          <Button shape="round" size="large" onClick={onFechar} disabled={salvando}>
            Cancelar
          </Button>
          <Button shape="round" size="large" type="primary" onClick={salvar} loading={salvando}>
            {ajuste ? 'Confirmar ajuste' : 'Registrar'}
          </Button>
        </div>
      }
    >
      {erro && (
        <div role="alert" style={{ background: '#FBEAEA', borderRadius: 10, padding: '12px 16px', color: '#8E2B20', margin: '16px 0' }}>
          {erro}
        </div>
      )}

      <Form
        form={form}
        layout="vertical"
        initialValues={{ insumoId: insumoId ?? undefined, tipo: 'ENTRADA' }}
        style={{ marginTop: 20 }}
      >
        <Form.Item name="insumoId" label="Insumo" rules={[{ required: true, message: 'Informe o insumo.' }]}>
          <Select
            size="large"
            showSearch
            optionFilterProp="label"
            placeholder="Escolha um insumo"
            options={insumos.map((i) => ({ value: i.id, label: i.ativo ? i.nome : `${i.nome} (inativo)` }))}
          />
        </Form.Item>

        <Form.Item name="tipo" label="Tipo" rules={[{ required: true, message: 'Informe o tipo da movimentação.' }]}>
          <Select
            size="large"
            options={(['ENTRADA', 'RETIRADA', 'AJUSTE'] as const).map((t) => ({ value: t, label: ROTULOS_TIPO[t] }))}
          />
        </Form.Item>

        <Form.Item
          name="quantidade"
          label={ajuste ? 'Quantidade contada' : 'Quantidade'}
          rules={[{ required: true, message: ajuste ? 'Informe a quantidade contada.' : 'Informe a quantidade.' }]}
          extra={
            escolhido && (
              <>
                Saldo no sistema: {quantidade(escolhido.quantidadeAtual)} {unidade}
                {diferenca !== null && ` · Diferença: ${quantidade(diferenca, true)} ${unidade}`}
              </>
            )
          }
        >
          <InputNumber
            size="large"
            min={ajuste ? 0 : 0.001}
            precision={3}
            decimalSeparator=","
            placeholder="0"
            suffix={unidade}
            style={{ width: '100%' }}
          />
        </Form.Item>

        {ajuste && (
          <Form.Item name="motivo" label="Motivo" rules={[{ required: true, message: 'Informe o motivo do ajuste.' }]}>
            <Select
              size="large"
              placeholder="Selecione o motivo"
              options={MOTIVOS.map((m) => ({ value: m, label: ROTULOS_MOTIVO[m] }))}
            />
          </Form.Item>
        )}
      </Form>

      <p style={{ color: '#6B645C', fontSize: 12, margin: 0 }}>
        {ajuste
          ? 'Informe quanto há de fato no estoque: o saldo passa a ser essa contagem.'
          : 'A quantidade usa a unidade do insumo.'}
      </p>
    </Modal>
  )
}

export default ModalMovimentacao
