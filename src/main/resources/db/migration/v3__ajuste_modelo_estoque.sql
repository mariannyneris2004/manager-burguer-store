-- Normaliza as tabelas antigas para trabalhar com entidades que usam id próprio
-- e com o fluxo: compra -> estoque -> pedido -> CMV.

ALTER TABLE ingrediente
    MODIFY medida_unitaria VARCHAR(50) NULL,
    MODIFY valor DECIMAL(10,2) NULL DEFAULT 0;

ALTER TABLE compra_item
    DROP PRIMARY KEY,
    ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD UNIQUE KEY uk_compra_item_compra_ingrediente (compra_id, ingrediente_id);

ALTER TABLE adicional_ingrediente
    DROP PRIMARY KEY,
    ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD UNIQUE KEY uk_adicional_ingrediente (adicional_id, ingrediente_id);

ALTER TABLE produto_ingrediente
    DROP PRIMARY KEY,
    ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD UNIQUE KEY uk_produto_ingrediente (produto_id, ingrediente_id);

ALTER TABLE pedido_adicional
    DROP PRIMARY KEY,
    ADD COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD UNIQUE KEY uk_pedido_adicional (pedido_id, adicional_id);
