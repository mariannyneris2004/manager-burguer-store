package com.bitokas.manager.model.gastos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "despesa_geral")
@Getter
@Setter
public class DespesaGeral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column
    private Double valor;

    @Column(name = "data_despesa")
    private Date dataDespesa;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequencia")
    private Frequencia frequencia;
}