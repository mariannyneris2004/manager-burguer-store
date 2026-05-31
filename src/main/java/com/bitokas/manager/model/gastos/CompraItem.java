package com.bitokas.manager.model.gastos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "compra_item")
@Getter
@Setter
public class CompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compra_id", nullable = false)
    private Long compraId;

    @Column(name = "ingrediente_id", nullable = false)
    private Long ingredienteId;

    /**
     * Quantidade comprada na unidade comercial usada na compra.
     * Ex.: 1 pacote, 2 caixas, 3 fardos.
     */
    @Column(nullable = false)
    private Double quantidade;

    /**
     * Quantas unidades de estoque existem em cada unidade de compra.
     * Ex.: 1 pacote de pão = 6 pães.
     */
    @Column(name = "unidades_por_embalagem", nullable = false)
    private Double unidadesPorEmbalagem;

    /**
     * Quantidade efetivamente lançada no estoque.
     * Ex.: 1 pacote x 6 unidades = 6 unidades de estoque.
     */
    @Column(name = "quantidade_estoque", nullable = false)
    private Double quantidadeEstoque;

    /**
     * Valor pago pela unidade de compra.
     * Ex.: valor de 1 pacote.
     */
    @Column(name = "valor_unitario", nullable = false)
    private Double valorUnitario;

    /**
     * Custo por unidade de estoque após a conversão.
     * Ex.: R$ 8 / 6 pães = R$ 1,33 por pão.
     */
    @Column(name = "custo_unitario_estoque", nullable = false)
    private Double custoUnitarioEstoque;

    @Column(name = "valor_total_item", nullable = false)
    private Double valorTotalItem;
}
