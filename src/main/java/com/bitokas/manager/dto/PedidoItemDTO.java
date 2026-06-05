package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItemDTO {
    private Long id;
    private Long pedidoId;
    private Long produtoId;
    private String produtoNome;
    private Double produtoValorVenda;
    private Integer quantidade;
    private Double custoBaseUnitario;
    private Double custoBaseTotal;
    private Double valorAdicionaisTotal;
    private Double custoAdicionaisTotal;
    private Double custoRetiradasTotal;
    private Double valorTotalItem;
    private Double custoTotalItem;
    private Double lucroItem;
    private List<PedidoItemAdicionalDTO> adicionais = new ArrayList<>();
    private List<PedidoItemRetiradaDTO> retiradas = new ArrayList<>();
}
