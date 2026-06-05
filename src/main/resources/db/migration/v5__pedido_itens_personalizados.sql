CREATE TABLE pedido_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    produto_nome VARCHAR(150) NOT NULL,
    produto_valor_venda DECIMAL(10,2) NOT NULL,
    quantidade INT NOT NULL,
    custo_base_unitario DECIMAL(10,4) NOT NULL,
    custo_base_total DECIMAL(10,2) NOT NULL,
    valor_adicionais_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    custo_adicionais_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    custo_retiradas_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    valor_total_item DECIMAL(10,2) NOT NULL,
    custo_total_item DECIMAL(10,2) NOT NULL,
    lucro_item DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_pedido_item_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE
);

CREATE TABLE pedido_item_adicional (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_item_id BIGINT NOT NULL,
    adicional_id BIGINT NOT NULL,
    adicional_nome VARCHAR(150) NOT NULL,
    adicional_valor_base DECIMAL(10,2) NOT NULL,
    quantidade INT NOT NULL,
    custo_unitario DECIMAL(10,4) NOT NULL,
    custo_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_pedido_item_adicional_item FOREIGN KEY (pedido_item_id) REFERENCES pedido_item(id) ON DELETE CASCADE
);

CREATE TABLE pedido_item_retirada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_item_id BIGINT NOT NULL,
    ingrediente_id BIGINT NOT NULL,
    ingrediente_nome VARCHAR(150) NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL,
    custo_unitario DECIMAL(10,4) NOT NULL,
    custo_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_pedido_item_retirada_item FOREIGN KEY (pedido_item_id) REFERENCES pedido_item(id) ON DELETE CASCADE
);
