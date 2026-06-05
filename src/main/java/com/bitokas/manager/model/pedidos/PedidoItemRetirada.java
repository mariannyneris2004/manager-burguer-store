package com.bitokas.manager.model.pedidos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pedido_item_retirada")
@Getter
@Setter
public class PedidoItemRetirada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_item_id", nullable = false)
    private Long pedidoItemId;

    @Column(name = "ingrediente_id", nullable = false)
    private Long ingredienteId;

    @Column(name = "ingrediente_nome", nullable = false)
    private String ingredienteNome;

    @Column(nullable = false)
    private Double quantidade;

    @Column(name = "custo_unitario", nullable = false)
    private Double custoUnitario;

    @Column(name = "custo_total", nullable = false)
    private Double custoTotal;
}
