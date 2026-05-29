package com.bitokas.manager.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IngredienteDTO {
    private Long id;
    private String nome;
    private String marca;
    private String medidaUnitaria;
    private Double valor;
}