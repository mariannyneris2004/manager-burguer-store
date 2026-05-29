package com.bitokas.manager.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CompraItemDTO {
    private Long id;
    private Long compraId;
    private Long ingredienteId;
    private Double quantidade;
    private Double valorUnitario;
}