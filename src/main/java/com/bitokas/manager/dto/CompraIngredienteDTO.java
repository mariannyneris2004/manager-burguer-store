package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraIngredienteDTO {
    private Long id;
    private Double valorTotal;
    private Date dataCompra;
    private List<CompraItemDTO> itens;
}