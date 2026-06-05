package com.bitokas.manager.dto;

import com.bitokas.manager.model.pedidos.TipoEntrega;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private String nomeCliente;
    private LocalDateTime dataHora;
    private TipoEntrega tipoEntrega;
    private Double valorEntrega;
    private Double valorTotal;
    private Double valorPago;
    private Double custoTotal;
    private Double lucroBruto;
    private List<PedidoItemDTO> itens = new ArrayList<>();
}
