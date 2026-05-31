package com.bitokas.manager.model.gastos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimento_estoque")
@Getter
@Setter
public class MovimentoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingrediente_id", nullable = false)
    private Long ingredienteId;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentoEstoque tipo;

    @Column(nullable = false)
    private Double quantidade;

    @Column(name = "custo_unitario", nullable = false)
    private Double custoUnitario;

    @Column(name = "valor_total", nullable = false)
    private Double valorTotal;

    @Column(name = "origem_tipo", nullable = false)
    private String origemTipo;

    @Column(name = "origem_id", nullable = false)
    private Long origemId;

    @Column
    private String observacao;
}
