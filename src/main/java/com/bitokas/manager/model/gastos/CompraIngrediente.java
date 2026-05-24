package com.bitokas.manager.model.gastos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "compra_ingrediente")
@Getter
@Setter
public class CompraIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valor_total")
    private Double valorTotal;

    @Column(name = "data_compra")
    private Date dataCompra;
}