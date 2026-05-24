package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdicionalDTO {
    private Long id;
    private String nome;
    private Double valorBase;
    private List<AdicionalIngredienteDTO> ingredientes;
}