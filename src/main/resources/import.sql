-- Insere ingredientes
INSERT INTO Ingrediente(id, nome) VALUES (1, 'Queijo Parmesão');
INSERT INTO Ingrediente(id, nome) VALUES (2, 'Ovo');
INSERT INTO Ingrediente(id, nome) VALUES (3, 'Bacon');
INSERT INTO Ingrediente(id, nome) VALUES (4, 'Massa de Spaghetti');
INSERT INTO Ingrediente(id, nome) VALUES (5, 'Salmão Fresco');
INSERT INTO Ingrediente(id, nome) VALUES (6, 'Arroz Japonês');
INSERT INTO Ingrediente(id, nome) VALUES (7, 'Alga Nori');
INSERT INTO Ingrediente(id, nome) VALUES (8, 'Feijão Preto');
INSERT INTO Ingrediente(id, nome) VALUES (9, 'Carne Seca');
INSERT INTO Ingrediente(id, nome) VALUES (10, 'Farinha de Mandioca');

-- Insere chefs
INSERT INTO Chef(id, nome, especialidade) VALUES (101, 'Massimo Bottura', 'Cozinha Italiana');
INSERT INTO Chef(id, nome, especialidade) VALUES (102, 'Alex Atala', 'Cozinha Brasileira');
INSERT INTO Chef(id, nome, especialidade) VALUES (103, 'Nobu Matsuhisa', 'Cozinha Japonesa');

-- Insere restaurantes e associa os chefs
INSERT INTO Restaurante(id, nome, endereco, chef_id) VALUES (1, 'Cantina do Massimo', 'Rua da Itália, 123', 101);
INSERT INTO Restaurante(id, nome, endereco, chef_id) VALUES (2, 'Brasil a Gosto', 'Avenida Brasil, 456', 102);
INSERT INTO Restaurante(id, nome, endereco, chef_id) VALUES (3, 'Sushi Leblon', 'Avenida dos Samurais, 789', 103);

-- Insere pratos e associa aos restaurantes
INSERT INTO Prato(id, nome, preco, categoria, restaurante_id) VALUES (201, 'Spaghetti Carbonara', 79.90, 'PRATO_PRINCIPAL', 1);
INSERT INTO Prato(id, nome, preco, categoria, restaurante_id) VALUES (202, 'Tiramisu', 35.50, 'SOBREMESA', 1);
INSERT INTO Prato(id, nome, preco, categoria, restaurante_id) VALUES (203, 'Feijoada Completa', 95.00, 'PRATO_PRINCIPAL', 2);
INSERT INTO Prato(id, nome, preco, categoria, restaurante_id) VALUES (204, 'Combinado de Sushi (20 peças)', 120.00, 'PRATO_PRINCIPAL', 3);

-- Associa ingredientes aos pratos (tabela Many-to-Many)
INSERT INTO prato_ingrediente(prato_id, ingrediente_id) VALUES (201, 1); -- Carbonara -> Parmesão
INSERT INTO prato_ingrediente(prato_id, ingrediente_id) VALUES (201, 2); -- Carbonara -> Ovo
INSERT INTO prato_ingrediente(prato_id, ingrediente_id) VALUES (201, 3); -- Carbonara -> Bacon
INSERT INTO prato_ingrediente(prato_id, ingrediente_id) VALUES (201, 4); -- Carbonara -> Massa
INSERT INTO prato_ingrediente(prato_id, ingrediente_id) VALUES (203, 8); -- Feijoada -> Feijão
INSERT INTO prato_ingredente(prato_id, ingrediente_id) VALUES (203, 9); -- Feijoada -> Carne Seca
INSERT INTO prato_ingrediente(prato_id, ingrediente_id) VALUES (204, 5); -- Sushi -> Salmão
INSERT INTO prato_ingrediente(prato_id, ingrediente_id) VALUES (204, 6); -- Sushi -> Arroz
INSERT INTO prato_ingrediente(prato_id, ingrediente_id) VALUES (204, 7); -- Sushi -> Alga