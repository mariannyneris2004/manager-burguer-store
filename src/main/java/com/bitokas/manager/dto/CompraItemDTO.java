package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraItemDTO {
    private Long id;
    private Long compraId;
    private Long ingredienteId;
    private Double quantidade;
    private Double valorUnitario;
}