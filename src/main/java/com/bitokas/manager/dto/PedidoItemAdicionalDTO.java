package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItemAdicionalDTO {
    private Long id;
    private Long pedidoItemId;
    private Long adicionalId;
    private String adicionalNome;
    private Double adicionalValorBase;
    private Integer quantidade;
    private Double custoUnitario;
    private Double custoTotal;
}
