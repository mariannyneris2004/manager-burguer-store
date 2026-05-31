package com.bitokas.manager.model.produtos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ingrediente")
@Getter
@Setter
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "marca")
    private String marca;

    @Column(name = "unidade_consumo")
    private String unidadeConsumo; // UNIDADE, G, ML, FATIA etc.

    @Column(name = "unidade_compra")
    private String unidadeCompra; // PACOTE, CAIXA etc.

    @Column(name = "quantidade_por_unidade_compra")
    private Double quantidadePorUnidadeCompra;
}
