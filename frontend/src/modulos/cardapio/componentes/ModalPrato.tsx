import { useEffect, useState } from 'react'
import { Alert, Button, Form, Input, InputNumber, Modal, Select } from 'antd'
import { errosDeCampo, mensagemDeErro } from '../../../compartilhado/servicos/api'
import { cadastrarPrato, editarPrato } from '../servicos/pratoServico'
import type { CategoriaCardapio, Prato, PratoEntrada } from '../servicos/pratoServico'

type ModalPratoProps = {
  aberto: boolean
  prato: Prato | null // null = cadastro; preenchido = edição
  categorias: CategoriaCardapio[]
  onFechar: () => void
  onSalvo: (prato: Prato) => void
}

type CamposFormulario = {
  nome: string
  descricao?: string
  categoriaCardapioId?: number
  precoVenda?: number
}

function ModalPrato({ aberto, prato, categorias, onFechar, onSalvo }: ModalPratoProps) {
  const [form] = Form.useForm<CamposFormulario>()
  const [salvando, setSalvando] = useState(false)
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const editando = prato !== null

  // Ao abrir, preenche com os dados do prato (edição) ou limpa (cadastro)
  useEffect(() => {
    if (!aberto) return
    if (prato) {
      form.setFieldsValue({
        nome: prato.nome,
        descricao: prato.descricao ?? undefined,
        categoriaCardapioId: prato.categoria.id,
        precoVenda: prato.precoVenda,
      })
    } else {
      form.resetFields()
    }
  }, [aberto, prato, form])

  function fechar() {
    setErroGeral(null)
    onFechar()
  }

  async function salvar(valores: CamposFormulario) {
    setSalvando(true)
    setErroGeral(null)
    const dados: PratoEntrada = {
      nome: valores.nome,
      descricao: valores.descricao,
      precoVenda: valores.precoVenda ?? 0,
      categoriaCardapioId: valores.categoriaCardapioId ?? 0,
    }
    try {
      const salvo = editando ? await editarPrato(prato.id, dados) : await cadastrarPrato(dados)
      setErroGeral(null)
      onSalvo(salvo)
    } catch (erro) {
      // 400: marca cada campo inválido; outros erros (ex.: 422 CATEGORIA_INATIVA): aviso no topo
      const campos = errosDeCampo(erro)
      if (campos.length > 0) {
        form.setFields(campos.map((c) => ({ name: c.campo as keyof CamposFormulario, errors: [c.mensagem] })))
      } else {
        setErroGeral(mensagemDeErro(erro))
      }
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={aberto}
      title={<span style={{ fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 30 }}>{editando ? 'Editar prato' : 'Novo prato'}</span>}
      onCancel={fechar}
      width={560}
      destroyOnHidden
      footer={[
        <Button key="cancelar" shape="round" onClick={fechar}>
          Cancelar
        </Button>,
        <Button key="salvar" shape="round" type="primary" loading={salvando} onClick={() => form.submit()}>
          {editando ? 'Salvar alterações' : 'Cadastrar prato'}
        </Button>,
      ]}
    >
      {erroGeral && <Alert type="error" message={erroGeral} showIcon style={{ marginBottom: 16 }} />}

      <Form form={form} layout="vertical" onFinish={salvar} requiredMark>
        <Form.Item name="nome" label="Nome do prato" rules={[{ required: true, whitespace: true, message: 'Informe o nome.' }, { max: 100, message: 'O nome pode ter no máximo 100 caracteres.' }]}>
          <Input placeholder="Ex.: Hambúrguer artesanal" size="large" />
        </Form.Item>

        <Form.Item name="descricao" label="Descrição" rules={[{ max: 500, message: 'A descrição pode ter no máximo 500 caracteres.' }]}>
          <Input.TextArea placeholder="Ingredientes, tamanho da porção ou outras informações" rows={3} />
        </Form.Item>

        <div style={{ display: 'flex', gap: 12 }}>
          <Form.Item name="categoriaCardapioId" label="Categoria" style={{ flex: 1 }} rules={[{ required: true, message: 'Informe a categoria do prato.' }]}>
            <Select
              placeholder="Selecione a categoria"
              size="large"
              options={categorias.map((c) => ({ value: c.id, label: c.nome }))}
            />
          </Form.Item>

          <Form.Item name="precoVenda" label="Preço de venda" style={{ flex: 1 }} rules={[{ required: true, message: 'Informe o preço de venda.' }]}>
            <InputNumber<number> prefix="R$" min={0} precision={2} decimalSeparator="," placeholder="0,00" size="large" style={{ width: '100%' }} />
          </Form.Item>
        </div>

        {!editando && (
          <p style={{ margin: 0, fontSize: 12, color: '#6B645C' }}>
            O prato é cadastrado como ativo. Para inativar, use o menu ⋯ na lista.
          </p>
        )}
      </Form>
    </Modal>
  )
}

export default ModalPrato
