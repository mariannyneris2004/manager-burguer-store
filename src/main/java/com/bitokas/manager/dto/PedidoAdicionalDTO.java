package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAdicionalDTO {
    private Long id;
    private Long pedidoId;
    private Long adicionalId;
    private Integer quantidade;

    private Boolean selecionado;
}