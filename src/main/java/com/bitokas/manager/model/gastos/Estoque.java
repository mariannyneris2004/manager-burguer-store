package com.bitokas.manager.model.gastos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "estoque",
        uniqueConstraints = @UniqueConstraint(columnNames = "ingrediente_id")
)
@Getter
@Setter
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingrediente_id", nullable = false)
    private Long ingredienteId;

    @Column(nullable = false)
    private Double quantidade;

    @Column(name = "custo_medio_atual", nullable = false)
    private Double custoMedioAtual;
}
