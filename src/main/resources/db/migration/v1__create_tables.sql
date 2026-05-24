CREATE TABLE ingrediente (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             nome VARCHAR(100) NOT NULL,
                             medida_unitaria VARCHAR(50) NOT NULL,
                             marca VARCHAR(100),
                             valor DECIMAL(10,2) NOT NULL
);

CREATE TABLE estoque (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         ingrediente_id BIGINT NOT NULL,
                         quantidade DECIMAL(10,3) NOT NULL DEFAULT 0,
                         FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id)
);

CREATE TABLE adicional (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nome VARCHAR(100) NOT NULL,
                           valor_base DECIMAL(10,2) NOT NULL
);

CREATE TABLE adicional_ingrediente (
                                       adicional_id BIGINT NOT NULL,
                                       ingrediente_id BIGINT NOT NULL,
                                       quantidade DECIMAL(10,3) NOT NULL,
                                       PRIMARY KEY (adicional_id, ingrediente_id),
                                       FOREIGN KEY (adicional_id) REFERENCES adicional(id),
                                       FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id)
);

CREATE TABLE produto (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         nome VARCHAR(100) NOT NULL,
                         categoria VARCHAR(100) NOT NULL,
                         valor_base DECIMAL(10,2) NOT NULL
);

CREATE TABLE produto_ingrediente (
                                     produto_id BIGINT NOT NULL,
                                     ingrediente_id BIGINT NOT NULL,
                                     quantidade DECIMAL(10,3) NOT NULL,
                                     PRIMARY KEY (produto_id, ingrediente_id),
                                     FOREIGN KEY (produto_id) REFERENCES produto(id),
                                     FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id)
);

CREATE TABLE produto_adicional (
                                   produto_id BIGINT NOT NULL,
                                   adicional_id BIGINT NOT NULL,
                                   PRIMARY KEY (produto_id, adicional_id),
                                   FOREIGN KEY (produto_id) REFERENCES produto(id),
                                   FOREIGN KEY (adicional_id) REFERENCES adicional(id)
);

CREATE TABLE despesa_geral (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               nome VARCHAR(100) NOT NULL,
                               valor DECIMAL(10,2) NOT NULL,
                               frequencia ENUM('UNICA','MENSAL','SEMANAL','DIARIA') NOT NULL,
                               data_despesa DATE NOT NULL
);

CREATE TABLE compra_ingrediente (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    valor_total DECIMAL(10,2) NOT NULL,
                                    data_compra DATE NOT NULL
);

CREATE TABLE compra_item (
                             compra_id BIGINT NOT NULL,
                             ingrediente_id BIGINT NOT NULL,
                             quantidade DECIMAL(10,3) NOT NULL,
                             valor_unitario DECIMAL(10,2) NOT NULL,
                             PRIMARY KEY (compra_id, ingrediente_id),
                             FOREIGN KEY (compra_id) REFERENCES compra_ingrediente(id),
                             FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id)
);

CREATE TABLE pedido (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        nome_cliente VARCHAR(100),
                        data_hora DATETIME NOT NULL,
                        tipo_entrega ENUM('ENTREGA', 'RETIRADA') NOT NULL,
                        valor_entrega DECIMAL(10,2),
                        valor_total DECIMAL(10,2) NOT NULL,
                        valor_pago DECIMAL(10,2) NOT NULL
);

CREATE TABLE pedido_produto (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                pedido_id BIGINT NOT NULL,
                                produto_id BIGINT NOT NULL,
                                quantidade INT NOT NULL,
                                FOREIGN KEY (pedido_id) REFERENCES pedido(id),
                                FOREIGN KEY (produto_id) REFERENCES produto(id)
);

CREATE TABLE pedido_adicional (
                                  pedido_id BIGINT NOT NULL,
                                  adicional_id BIGINT NOT NULL,
                                  quantidade INT NOT NULL,
                                  PRIMARY KEY (pedido_id, adicional_id),
                                  FOREIGN KEY (pedido_id) REFERENCES pedido(id),
                                  FOREIGN KEY (adicional_id) REFERENCES adicional(id)
);