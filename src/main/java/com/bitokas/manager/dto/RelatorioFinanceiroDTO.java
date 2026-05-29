package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioFinanceiroDTO {
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private Double totalVendas;
    private Double totalCompras;
    private Double totalDespesas;
    private Double lucro;

    public RelatorioFinanceiroDTO(LocalDateTime inicio, LocalDateTime fim) {
        this.inicio = inicio;
        this.fim = fim;
    }
}