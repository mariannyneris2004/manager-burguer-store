package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {
    private Long id;
    private String nome;
    private Double valorVenda;
    private String categoria;
    private Double custoUnitario;
    private Double lucroUnitario;
    private Double margemPercentual;
    private List<ProdutoIngredienteDTO> ingredientes;
    private List<ProdutoAdicionalDTO> adicionaisPermitidos;
}