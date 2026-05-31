package com.bitokas.manager.model.pedidos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedido")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "tipo_entrega")
    @Enumerated(EnumType.STRING)
    private TipoEntrega tipoEntrega;

    @Column(name = "valor_entrega")
    private Double valorEntrega;

    @Column(name = "valor_total")
    private Double valorTotal;

    @Column(name = "valor_pago")
    private Double valorPago;

    @Column(name = "custo_total")
    private Double custoTotal;

    @Column(name = "lucro_bruto")
    private Double lucroBruto;
}