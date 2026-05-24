package com.bitokas.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoAdicionalDTO {
    private Long id;
    private Long produtoId;
    private Long adicionalId;
}