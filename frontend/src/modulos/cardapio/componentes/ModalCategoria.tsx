import { useEffect, useState } from 'react'
import { Alert, Button, Form, Input, Modal } from 'antd'
import { errosDeCampo, mensagemDeErro } from '../../../compartilhado/servicos/api'
import { cadastrarCategoria, editarCategoria } from '../servicos/categoriaServico'
import type { Categoria } from '../servicos/categoriaServico'

type ModalCategoriaProps = {
  aberto: boolean
  categoria: Categoria | null // null = cadastro; preenchida = edição
  onFechar: () => void
  onSalva: (categoria: Categoria) => void
}

type CamposFormulario = {
  nome: string
}

function ModalCategoria({ aberto, categoria, onFechar, onSalva }: ModalCategoriaProps) {
  const [form] = Form.useForm<CamposFormulario>()
  const [salvando, setSalvando] = useState(false)
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const editando = categoria !== null

  // Ao abrir, preenche com o nome da categoria (edição) ou limpa (cadastro)
  useEffect(() => {
    if (!aberto) return
    if (categoria) {
      form.setFieldsValue({ nome: categoria.nome })
    } else {
      form.resetFields()
    }
  }, [aberto, categoria, form])

  function fechar() {
    setErroGeral(null)
    onFechar()
  }

  async function salvar(valores: CamposFormulario) {
    setSalvando(true)
    setErroGeral(null)
    try {
      const salva = editando ? await editarCategoria(categoria.id, valores) : await cadastrarCategoria(valores)
      onSalva(salva)
    } catch (erro) {
      // 400: marca o campo inválido; outros erros (ex.: 409 CATEGORIA_JA_CADASTRADA): aviso no topo
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
      title={<span style={{ fontFamily: "'Jost', sans-serif", fontWeight: 300, fontSize: 30 }}>{editando ? 'Editar categoria' : 'Nova categoria'}</span>}
      onCancel={fechar}
      width={480}
      destroyOnHidden
      footer={[
        <Button key="cancelar" shape="round" onClick={fechar}>
          Cancelar
        </Button>,
        <Button key="salvar" shape="round" type="primary" loading={salvando} onClick={() => form.submit()}>
          {editando ? 'Salvar alterações' : 'Cadastrar categoria'}
        </Button>,
      ]}
    >
      {erroGeral && <Alert type="error" message={erroGeral} showIcon style={{ marginBottom: 16 }} />}

      <Form form={form} layout="vertical" onFinish={salvar} requiredMark>
        <Form.Item name="nome" label="Nome da categoria" rules={[{ required: true, whitespace: true, message: 'Informe o nome.' }, { max: 100, message: 'O nome pode ter no máximo 100 caracteres.' }]}>
          <Input placeholder="Ex.: Bebidas" size="large" />
        </Form.Item>

        {!editando && (
          <p style={{ margin: 0, fontSize: 12, color: '#6B645C' }}>
            A categoria é cadastrada como ativa. Para inativar ou excluir, use o menu ⋯ na lista.
          </p>
        )}
      </Form>
    </Modal>
  )
}

export default ModalCategoria
