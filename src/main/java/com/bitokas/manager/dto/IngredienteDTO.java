package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteDTO {
    private Long id;
    private String nome;
    private String marca;
    private String unidadeConsumo;
    private String unidadeCompra;
    private Double quantidadePorUnidadeCompra;
    private Double estoqueAtual;
    private Double custoMedioAtual;
    private Double valorTotalEstoque;
}
