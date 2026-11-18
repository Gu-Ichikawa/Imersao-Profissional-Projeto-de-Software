-- =====================================================================
-- SCRUM-111 - Tabelas do protótipo (Cardápio + Estoque)
-- Segue o DER da 3ª entrega (Miro). Divergências registradas no backend/README.md
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuários e permissões (versão mínima: só para o responsável das movimentações)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    login VARCHAR(100) NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_login UNIQUE (login)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
-- Estoque (DER Estoque: Almoxarifado)
-- ---------------------------------------------------------------------
CREATE TABLE categoria_insumo (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    CONSTRAINT pk_categoria_insumo PRIMARY KEY (id),
    CONSTRAINT uk_categoria_insumo_nome UNIQUE (nome)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE insumo (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    nome                VARCHAR(100)  NOT NULL,
    sku                 VARCHAR(50)   NULL,
    unidade_medida      VARCHAR(10)   NOT NULL,
    perecivel           BIT(1)        NOT NULL DEFAULT b'0',
    estoque_minimo      DECIMAL(12,3) NOT NULL DEFAULT 0,
    custo_unitario      DECIMAL(12,4) NOT NULL DEFAULT 0,
    controla_estoque    BIT(1)        NOT NULL DEFAULT b'1',
    ativo               BIT(1)        NOT NULL DEFAULT b'1',
    quantidade_atual    DECIMAL(12,3) NOT NULL DEFAULT 0,  -- divergência: saldo no insumo (sem lotes)
    categoria_insumo_id BIGINT        NOT NULL,
    CONSTRAINT pk_insumo PRIMARY KEY (id),
    CONSTRAINT uk_insumo_sku UNIQUE (sku),
    CONSTRAINT fk_insumo_categoria_insumo FOREIGN KEY (categoria_insumo_id) REFERENCES categoria_insumo (id),
    CONSTRAINT ck_insumo_unidade CHECK (unidade_medida IN ('KG', 'G', 'L', 'ML', 'UNIDADE')),
    CONSTRAINT ck_insumo_valores CHECK (estoque_minimo >= 0 AND custo_unitario >= 0 AND quantidade_atual >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE movimentacao_estoque (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    tipo           VARCHAR(20)   NOT NULL,
    quantidade     DECIMAL(12,3) NOT NULL,
    data_hora      DATETIME(6)   NOT NULL,
    insumo_id      BIGINT        NOT NULL,
    lote_id        BIGINT        NULL,      -- sem FK: lotes ficam para uma próxima entrega
    responsavel_id BIGINT        NOT NULL,
    comanda_id     BIGINT        NULL,      -- sem FK: o módulo Caixa ainda não existe
    CONSTRAINT pk_movimentacao_estoque PRIMARY KEY (id),
    CONSTRAINT fk_movimentacao_insumo FOREIGN KEY (insumo_id) REFERENCES insumo (id),
    CONSTRAINT fk_movimentacao_responsavel FOREIGN KEY (responsavel_id) REFERENCES usuario (id),
    CONSTRAINT ck_movimentacao_tipo CHECK (tipo IN ('ENTRADA', 'RETIRADA', 'AJUSTE', 'BAIXA_POR_VENDA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_movimentacao_insumo_data ON movimentacao_estoque (insumo_id, data_hora);

-- Ajuste de estoque: perda, quebra, vencimento e erro de contagem.
-- É uma especialização da movimentação (1 movimentação do tipo AJUSTE -> 0..1 ajuste).
CREATE TABLE ajuste_estoque (
    movimentacao_estoque_id BIGINT        NOT NULL,
    saldo_sistema           DECIMAL(12,3) NOT NULL,
    quantidade_contada      DECIMAL(12,3) NOT NULL,
    diferenca               DECIMAL(12,3) NOT NULL,  -- quantidade_contada - saldo_sistema (pode ser negativa)
    motivo                  VARCHAR(20)   NOT NULL,
    status                  VARCHAR(25)   NOT NULL DEFAULT 'EFETIVADO',
    aprovador_id            BIGINT        NULL,
    CONSTRAINT pk_ajuste_estoque PRIMARY KEY (movimentacao_estoque_id),
    CONSTRAINT fk_ajuste_movimentacao FOREIGN KEY (movimentacao_estoque_id) REFERENCES movimentacao_estoque (id),
    CONSTRAINT fk_ajuste_aprovador FOREIGN KEY (aprovador_id) REFERENCES usuario (id),
    CONSTRAINT ck_ajuste_motivo CHECK (motivo IN ('PERDA', 'QUEBRA', 'VENCIMENTO', 'ERRO_CONTAGEM')),
    CONSTRAINT ck_ajuste_status CHECK (status IN ('AGUARDANDO_APROVACAO', 'EFETIVADO', 'CANCELADO')),
    CONSTRAINT ck_ajuste_quantidades CHECK (saldo_sistema >= 0 AND quantidade_contada >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
-- Cardápio (DER Cardápio)
-- ---------------------------------------------------------------------
CREATE TABLE categoria_cardapio (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    nome  VARCHAR(100) NOT NULL,
    ativa BIT(1)       NOT NULL DEFAULT b'1',
    CONSTRAINT pk_categoria_cardapio PRIMARY KEY (id),
    CONSTRAINT uk_categoria_cardapio_nome UNIQUE (nome)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE prato (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    nome                  VARCHAR(100)  NOT NULL,
    descricao             VARCHAR(500)  NULL,
    preco_venda           DECIMAL(12,2) NOT NULL,
    taxa_cmv              DECIMAL(5,2)  NULL,   -- CMV fica fora desta entrega
    imagem_url            VARCHAR(500)  NULL,   -- imagem fica fora desta entrega
    ativo                 BIT(1)        NOT NULL DEFAULT b'1',
    categoria_cardapio_id BIGINT        NOT NULL,
    CONSTRAINT pk_prato PRIMARY KEY (id),
    CONSTRAINT fk_prato_categoria_cardapio FOREIGN KEY (categoria_cardapio_id) REFERENCES categoria_cardapio (id),
    CONSTRAINT ck_prato_preco CHECK (preco_venda >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE ficha_tecnica (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    custo_total   DECIMAL(12,2) NOT NULL DEFAULT 0,
    atualizada_em DATETIME(6)   NOT NULL,
    prato_id      BIGINT        NOT NULL,
    CONSTRAINT pk_ficha_tecnica PRIMARY KEY (id),
    CONSTRAINT uk_ficha_tecnica_prato UNIQUE (prato_id),
    CONSTRAINT fk_ficha_tecnica_prato FOREIGN KEY (prato_id) REFERENCES prato (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE item_ficha_tecnica (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    quantidade       DECIMAL(12,3) NOT NULL,
    ficha_tecnica_id BIGINT        NOT NULL,
    insumo_id        BIGINT        NOT NULL,
    CONSTRAINT pk_item_ficha_tecnica PRIMARY KEY (id),
    CONSTRAINT uk_item_ficha_insumo UNIQUE (ficha_tecnica_id, insumo_id),
    CONSTRAINT fk_item_ficha_ficha_tecnica FOREIGN KEY (ficha_tecnica_id) REFERENCES ficha_tecnica (id),
    CONSTRAINT fk_item_ficha_insumo FOREIGN KEY (insumo_id) REFERENCES insumo (id),
    CONSTRAINT ck_item_ficha_quantidade CHECK (quantidade > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
