ALTER TABLE ingrediente
    ADD COLUMN unidade_consumo VARCHAR(20) NOT NULL DEFAULT 'UNIDADE',
    ADD COLUMN unidade_compra VARCHAR(20) NOT NULL DEFAULT 'PACOTE',
    ADD COLUMN quantidade_por_unidade_compra DECIMAL(10,3) NOT NULL DEFAULT 1;

ALTER TABLE estoque
    ADD COLUMN custo_medio_atual DECIMAL(10,4) NOT NULL DEFAULT 0;

ALTER TABLE compra_item
    ADD COLUMN unidades_por_embalagem DECIMAL(10,3) NOT NULL DEFAULT 1,
    ADD COLUMN quantidade_estoque DECIMAL(10,3) NOT NULL DEFAULT 0,
    ADD COLUMN custo_unitario_estoque DECIMAL(10,4) NOT NULL DEFAULT 0,
    ADD COLUMN valor_total_item DECIMAL(10,2) NOT NULL DEFAULT 0;

ALTER TABLE pedido
    ADD COLUMN custo_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN lucro_bruto DECIMAL(10,2) NOT NULL DEFAULT 0;

CREATE TABLE movimento_estoque (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   ingrediente_id BIGINT NOT NULL,
                                   data_hora DATETIME NOT NULL,
                                   tipo VARCHAR(30) NOT NULL,
                                   quantidade DECIMAL(10,3) NOT NULL,
                                   custo_unitario DECIMAL(10,4) NOT NULL,
                                   valor_total DECIMAL(10,2) NOT NULL,
                                   origem_tipo VARCHAR(30) NOT NULL,
                                   origem_id BIGINT NOT NULL,
                                   observacao VARCHAR(255),
                                   FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id)
);