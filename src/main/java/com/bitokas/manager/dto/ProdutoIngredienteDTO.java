package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoIngredienteDTO {
    private Long id;
    private Long produtoId;
    private Long ingredienteId;
    private Double quantidade;

    private Boolean selecionado;
}