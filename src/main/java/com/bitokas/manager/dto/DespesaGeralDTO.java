package com.bitokas.manager.dto;

import com.bitokas.manager.model.gastos.Frequencia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DespesaGeralDTO {
    private Long id;
    private String nome;
    private Double valor;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dataDespesa;
    private Frequencia frequencia;
}