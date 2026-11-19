import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Modal, Select, Switch } from 'antd'
import axios from 'axios'
import { errosDeCampo, mensagemDeErro } from '../../../compartilhado/servicos/api'
import type { ErroApi } from '../../../compartilhado/servicos/api'
import { rotuloUnidade } from '../../../compartilhado/formatadores'
import { UNIDADES, cadastrarInsumo, editarInsumo, listarCategoriasInsumo } from '../servicos/insumoServico'
import type { CategoriaInsumo, Insumo, InsumoRequest, UnidadeMedida } from '../servicos/insumoServico'

const NOMES_UNIDADE: Record<UnidadeMedida, string> = {
  KG: 'Quilograma',
  G: 'Grama',
  L: 'Litro',
  ML: 'Mililitro',
  UNIDADE: 'Unidade',
}

type ValoresFormulario = {
  nome: string
  sku?: string
  categoriaInsumoId: number
  unidadeMedida: UnidadeMedida
  estoqueMinimo: number
  custoUnitario: number
  quantidadeInicial?: number | null
  controlaEstoque: boolean
  perecivel: boolean
}

type ModalInsumoProps = {
  // null = novo insumo
  insumo: Insumo | null
  onFechar: () => void
  onSalvo: (insumo: Insumo) => void
}

function codigoDeErro(erro: unknown): string | undefined {
  return axios.isAxiosError<ErroApi>(erro) ? erro.response?.data?.codigo : undefined
}

function ModalInsumo({ insumo, onFechar, onSalvo }: ModalInsumoProps) {
  const [form] = Form.useForm<ValoresFormulario>()
  const [categorias, setCategorias] = useState<CategoriaInsumo[]>([])
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    listarCategoriasInsumo()
      .then(setCategorias)
      .catch((e) => setErro(mensagemDeErro(e)))
  }, [])

  const valoresIniciais: Partial<ValoresFormulario> = insumo
    ? {
        nome: insumo.nome,
        sku: insumo.sku ?? undefined,
        categoriaInsumoId: insumo.categoria.id,
        unidadeMedida: insumo.unidadeMedida,
        estoqueMinimo: insumo.estoqueMinimo,
        custoUnitario: insumo.custoUnitario,
        controlaEstoque: insumo.controlaEstoque,
        perecivel: insumo.perecivel,
      }
    : { controlaEstoque: true, perecivel: false, quantidadeInicial: 0 }

  async function salvar() {
    let valores: ValoresFormulario
    try {
      valores = await form.validateFields()
    } catch {
      return
    }

    const dados: InsumoRequest = {
      nome: valores.nome.trim(),
      sku: valores.sku?.trim() ? valores.sku.trim() : null,
      unidadeMedida: valores.unidadeMedida,
      perecivel: valores.perecivel,
      estoqueMinimo: valores.estoqueMinimo,
      custoUnitario: valores.custoUnitario,
      controlaEstoque: valores.controlaEstoque,
      categoriaInsumoId: valores.categoriaInsumoId,
      ...(insumo ? {} : { quantidadeInicial: valores.quantidadeInicial ?? 0 }),
    }

    setSalvando(true)
    setErro(null)
    try {
      const salvo = insumo ? await editarInsumo(insumo.id, dados) : await cadastrarInsumo(dados)
      onSalvo(salvo)
    } catch (e) {
      const campos = errosDeCampo(e)
      if (campos.length > 0) {
        form.setFields(campos.map((c) => ({ name: c.campo as keyof ValoresFormulario, errors: [c.mensagem] })))
      }
      if (codigoDeErro(e) === 'SKU_JA_CADASTRADO') {
        form.setFields([{ name: 'sku', errors: [mensagemDeErro(e)] }])
      } else {
        setErro(mensagemDeErro(e))
      }
    } finally {
      setSalvando(false)
    }
  }

  const duasColunas = { display: 'grid', gridTemplateColumns: '1fr 1fr', columnGap: 12 }

  return (
    <Modal
      open
      centered
      width={560}
      onCancel={onFechar}
      maskClosable={!salvando}
      title={
        <span style={{ fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 30 }}>
          {insumo ? 'Editar insumo' : 'Novo insumo'}
        </span>
      }
      footer={
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
          <Button shape="round" size="large" onClick={onFechar} disabled={salvando}>
            Cancelar
          </Button>
          <Button shape="round" size="large" type="primary" onClick={salvar} loading={salvando}>
            {insumo ? 'Salvar alterações' : 'Cadastrar insumo'}
          </Button>
        </div>
      }
    >
      {erro && (
        <div role="alert" style={{ background: '#FBEAEA', borderRadius: 10, padding: '12px 16px', color: '#8E2B20', margin: '16px 0' }}>
          {erro}
        </div>
      )}

      <Form form={form} layout="vertical" initialValues={valoresIniciais} style={{ marginTop: 20 }}>
        <Form.Item
          name="nome"
          label="Nome do insumo"
          rules={[
            { required: true, whitespace: true, message: 'Informe o nome.' },
            { max: 100, message: 'O nome pode ter no máximo 100 caracteres.' },
          ]}
        >
          <Input size="large" placeholder="Ex.: Pão brioche" />
        </Form.Item>

        <div style={duasColunas}>
          <Form.Item name="sku" label="SKU" rules={[{ max: 50, message: 'O SKU pode ter no máximo 50 caracteres.' }]}>
            <Input size="large" placeholder="Ex.: INS-001" />
          </Form.Item>
          <Form.Item name="categoriaInsumoId" label="Categoria" rules={[{ required: true, message: 'Informe a categoria do insumo.' }]}>
            <Select
              size="large"
              placeholder="Selecione a categoria"
              options={categorias.map((c) => ({ value: c.id, label: c.nome }))}
            />
          </Form.Item>
        </div>

        <div style={duasColunas}>
          <Form.Item name="unidadeMedida" label="Unidade de medida" rules={[{ required: true, message: 'Informe a unidade de medida.' }]}>
            <Select
              size="large"
              placeholder="Selecione a unidade"
              options={UNIDADES.map((u) => ({ value: u, label: `${NOMES_UNIDADE[u]} (${rotuloUnidade(u)})` }))}
            />
          </Form.Item>
          <Form.Item name="estoqueMinimo" label="Estoque mínimo" rules={[{ required: true, message: 'Informe o estoque mínimo.' }]}>
            <InputNumber size="large" min={0} precision={3} decimalSeparator="," placeholder="0" style={{ width: '100%' }} />
          </Form.Item>
        </div>

        <div style={duasColunas}>
          <Form.Item name="custoUnitario" label="Custo unitário" rules={[{ required: true, message: 'Informe o custo unitário.' }]}>
            <InputNumber size="large" min={0} precision={4} decimalSeparator="," prefix="R$" placeholder="0,0000" style={{ width: '100%' }} />
          </Form.Item>
          {insumo ? (
            <Form.Item label="Saldo atual">
              <Input
                size="large"
                disabled
                value={`${insumo.quantidadeAtual.toLocaleString('pt-BR', { maximumFractionDigits: 3 })} ${rotuloUnidade(insumo.unidadeMedida)}`}
              />
            </Form.Item>
          ) : (
            <Form.Item name="quantidadeInicial" label="Quantidade inicial">
              <InputNumber size="large" min={0} precision={3} decimalSeparator="," placeholder="0" style={{ width: '100%' }} />
            </Form.Item>
          )}
        </div>

        <div style={duasColunas}>
          <Form.Item
            name="controlaEstoque"
            label="Controla estoque"
            valuePropName="checked"
            extra="Se desligado, não é baixado nas vendas."
          >
            <Switch />
          </Form.Item>
          <Form.Item name="perecivel" label="Perecível" valuePropName="checked" extra="Insumo com validade.">
            <Switch />
          </Form.Item>
        </div>
      </Form>

      <p style={{ color: '#6B645C', fontSize: 12, margin: 0 }}>
        Campos com * são obrigatórios.
        {insumo ? ' Para mudar o saldo, use a movimentação de estoque.' : ' Depois do cadastro, o saldo só muda por movimentação de estoque.'}
      </p>
    </Modal>
  )
}

export default ModalInsumo
