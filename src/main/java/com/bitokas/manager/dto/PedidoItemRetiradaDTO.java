package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItemRetiradaDTO {
    private Long id;
    private Long pedidoItemId;
    private Long ingredienteId;
    private String ingredienteNome;
    private Double quantidade;
    private Double custoUnitario;
    private Double custoTotal;
}
