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
    private Double unidadesPorEmbalagem;
    private Double quantidadeEstoque;
    private Double valorUnitario;
    private Double custoUnitarioEstoque;
    private Double valorTotalItem;
}
