-- SCRUM-107: dados de exemplo para a demo (mesmos dados das telas do Figma)

-- Categorias do cardápio
INSERT INTO categoria_cardapio (nome, ativa) VALUES
  ('Lanches', b'1'),
  ('Porções', b'1'),
  ('Pizzas', b'1'),
  ('Saladas', b'1'),
  ('Sobremesas', b'1');

-- Insumos (categoria buscada pelo nome, das categorias criadas no V2)
INSERT INTO insumo (nome, sku, unidade_medida, perecivel, estoque_minimo, custo_unitario, controla_estoque, ativo, quantidade_atual, categoria_insumo_id)
SELECT 'Pão brioche', 'INS-001', 'UNIDADE', b'1', 5, 1.2000, b'1', b'1', 10, id
FROM categoria_insumo WHERE nome = 'Pães e massas';

INSERT INTO insumo (nome, sku, unidade_medida, perecivel, estoque_minimo, custo_unitario, controla_estoque, ativo, quantidade_atual, categoria_insumo_id)
SELECT 'Carne bovina', 'INS-002', 'G', b'1', 1000, 0.0380, b'1', b'1', 2000, id
FROM categoria_insumo WHERE nome = 'Carnes';

INSERT INTO insumo (nome, sku, unidade_medida, perecivel, estoque_minimo, custo_unitario, controla_estoque, ativo, quantidade_atual, categoria_insumo_id)
SELECT 'Queijo cheddar', 'INS-003', 'UNIDADE', b'1', 10, 0.4500, b'1', b'1', 40, id
FROM categoria_insumo WHERE nome = 'Laticínios';

INSERT INTO insumo (nome, sku, unidade_medida, perecivel, estoque_minimo, custo_unitario, controla_estoque, ativo, quantidade_atual, categoria_insumo_id)
SELECT 'Alface americana', 'INS-004', 'G', b'1', 200, 0.0150, b'1', b'1', 500, id
FROM categoria_insumo WHERE nome = 'Hortifrúti';

INSERT INTO insumo (nome, sku, unidade_medida, perecivel, estoque_minimo, custo_unitario, controla_estoque, ativo, quantidade_atual, categoria_insumo_id)
SELECT 'Molho da casa', 'INS-005', 'ML', b'1', 300, 0.0100, b'1', b'1', 1000, id
FROM categoria_insumo WHERE nome = 'Molhos e temperos';

-- Pratos (categoria buscada pelo nome)
INSERT INTO prato (nome, descricao, preco_venda, taxa_cmv, imagem_url, ativo, categoria_cardapio_id)
SELECT 'Hambúrguer artesanal', 'Pão brioche, 150 g de carne, cheddar, alface e molho da casa', 32.00, 26.25, NULL, b'1', id
FROM categoria_cardapio WHERE nome = 'Lanches';

INSERT INTO prato (nome, descricao, preco_venda, taxa_cmv, imagem_url, ativo, categoria_cardapio_id)
SELECT 'Batata frita', NULL, 24.00, NULL, NULL, b'1', id FROM categoria_cardapio WHERE nome = 'Porções';

INSERT INTO prato (nome, descricao, preco_venda, taxa_cmv, imagem_url, ativo, categoria_cardapio_id)
SELECT 'Porção de calabresa', NULL, 38.00, NULL, NULL, b'1', id FROM categoria_cardapio WHERE nome = 'Porções';

INSERT INTO prato (nome, descricao, preco_venda, taxa_cmv, imagem_url, ativo, categoria_cardapio_id)
SELECT 'Pizza margherita', NULL, 54.00, NULL, NULL, b'1', id FROM categoria_cardapio WHERE nome = 'Pizzas';

INSERT INTO prato (nome, descricao, preco_venda, taxa_cmv, imagem_url, ativo, categoria_cardapio_id)
SELECT 'Salada caesar', NULL, 29.00, NULL, NULL, b'1', id FROM categoria_cardapio WHERE nome = 'Saladas';

INSERT INTO prato (nome, descricao, preco_venda, taxa_cmv, imagem_url, ativo, categoria_cardapio_id)
SELECT 'Brownie com sorvete', NULL, 22.00, NULL, NULL, b'1', id FROM categoria_cardapio WHERE nome = 'Sobremesas';

-- Ficha técnica do Hambúrguer artesanal (custo 8,40)
INSERT INTO ficha_tecnica (custo_total, atualizada_em, prato_id)
SELECT 8.40, NOW(6), id FROM prato WHERE nome = 'Hambúrguer artesanal';

INSERT INTO item_ficha_tecnica (quantidade, ficha_tecnica_id, insumo_id)
SELECT 1, f.id, i.id FROM ficha_tecnica f
JOIN prato p ON p.id = f.prato_id AND p.nome = 'Hambúrguer artesanal'
JOIN insumo i ON i.nome = 'Pão brioche';

INSERT INTO item_ficha_tecnica (quantidade, ficha_tecnica_id, insumo_id)
SELECT 150, f.id, i.id FROM ficha_tecnica f
JOIN prato p ON p.id = f.prato_id AND p.nome = 'Hambúrguer artesanal'
JOIN insumo i ON i.nome = 'Carne bovina';

INSERT INTO item_ficha_tecnica (quantidade, ficha_tecnica_id, insumo_id)
SELECT 2, f.id, i.id FROM ficha_tecnica f
JOIN prato p ON p.id = f.prato_id AND p.nome = 'Hambúrguer artesanal'
JOIN insumo i ON i.nome = 'Queijo cheddar';

INSERT INTO item_ficha_tecnica (quantidade, ficha_tecnica_id, insumo_id)
SELECT 20, f.id, i.id FROM ficha_tecnica f
JOIN prato p ON p.id = f.prato_id AND p.nome = 'Hambúrguer artesanal'
JOIN insumo i ON i.nome = 'Alface americana';

INSERT INTO item_ficha_tecnica (quantidade, ficha_tecnica_id, insumo_id)
SELECT 30, f.id, i.id FROM ficha_tecnica f
JOIN prato p ON p.id = f.prato_id AND p.nome = 'Hambúrguer artesanal'
JOIN insumo i ON i.nome = 'Molho da casa';

-- Histórico: uma ENTRADA por insumo, no dia anterior, pelo usuário de demonstração
INSERT INTO movimentacao_estoque (tipo, quantidade, data_hora, insumo_id, responsavel_id)
SELECT 'ENTRADA', quantidade_atual, NOW(6) - INTERVAL 1 DAY, id, 1 FROM insumo;
