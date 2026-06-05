CREATE TABLE pedido_item_consumo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_item_id BIGINT NOT NULL,
    ingrediente_id BIGINT NOT NULL,
    ingrediente_nome VARCHAR(150) NOT NULL,
    origem_tipo VARCHAR(30) NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL,
    custo_unitario DECIMAL(10,4) NOT NULL,
    custo_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_pedido_item_consumo_item FOREIGN KEY (pedido_item_id) REFERENCES pedido_item(id) ON DELETE CASCADE
);
