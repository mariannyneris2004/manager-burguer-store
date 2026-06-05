package com.bitokas.manager.model.pedidos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pedido_item_adicional")
@Getter
@Setter
public class PedidoItemAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_item_id", nullable = false)
    private Long pedidoItemId;

    @Column(name = "adicional_id", nullable = false)
    private Long adicionalId;

    @Column(name = "adicional_nome", nullable = false)
    private String adicionalNome;

    @Column(name = "adicional_valor_base", nullable = false)
    private Double adicionalValorBase;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "custo_unitario", nullable = false)
    private Double custoUnitario;

    @Column(name = "custo_total", nullable = false)
    private Double custoTotal;
}
