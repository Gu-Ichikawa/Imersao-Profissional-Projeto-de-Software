-- =====================================================================
-- SCRUM-111 - Dados iniciais obrigatórios (não são os dados da demo)
-- =====================================================================

-- Usuário fixo usado como responsável das movimentações enquanto não há login
INSERT INTO usuario (id, login) VALUES (1, 'demo');

INSERT INTO categoria_insumo (nome) VALUES
    ('Pães e massas'),
    ('Carnes'),
    ('Laticínios'),
    ('Hortifrúti'),
    ('Molhos e temperos'),
    ('Bebidas'),
    ('Embalagens');
