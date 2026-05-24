package com.bitokas.manager.model.produtos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "produto_ingrediente")
@Getter
@Setter
public class ProdutoIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "ingrediente_id")
    private Long ingredienteId;

    @Column(name = "quantidade")
    private Double quantidade;
}
