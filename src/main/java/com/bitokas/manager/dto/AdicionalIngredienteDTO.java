package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdicionalIngredienteDTO {
    private Long id;
    private Long adicionalId;
    private Long ingredienteId;
    private Double quantidade;
}