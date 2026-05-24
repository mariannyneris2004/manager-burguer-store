package com.bitokas.manager.model.produtos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "adicional_ingrediente")
@Getter
@Setter
public class AdicionalIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "adicional_id")
    private Long adicionalId;

    @Column(name = "ingrediente_id")
    private Long ingredienteId;

    @Column(name = "quantidade")
    private Double quantidade;
}
