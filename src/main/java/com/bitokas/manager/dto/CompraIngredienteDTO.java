package com.bitokas.manager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CompraIngredienteDTO {
    private Long id;
    private Double valorTotal;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dataCompra;

    @NotNull
    @Size(min = 1)
    private List<CompraItemDTO> itens;
}