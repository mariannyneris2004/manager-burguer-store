package com.bitokas.manager.model.pedidos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pedido_item")
@Getter
@Setter
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "produto_nome", nullable = false)
    private String produtoNome;

    @Column(name = "produto_valor_venda", nullable = false)
    private Double produtoValorVenda;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "custo_base_unitario", nullable = false)
    private Double custoBaseUnitario;

    @Column(name = "custo_base_total", nullable = false)
    private Double custoBaseTotal;

    @Column(name = "valor_adicionais_total", nullable = false)
    private Double valorAdicionaisTotal;

    @Column(name = "custo_adicionais_total", nullable = false)
    private Double custoAdicionaisTotal;

    @Column(name = "custo_retiradas_total", nullable = false)
    private Double custoRetiradasTotal;

    @Column(name = "valor_total_item", nullable = false)
    private Double valorTotalItem;

    @Column(name = "custo_total_item", nullable = false)
    private Double custoTotalItem;

    @Column(name = "lucro_item", nullable = false)
    private Double lucroItem;
}
