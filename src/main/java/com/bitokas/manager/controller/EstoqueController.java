package com.bitokas.manager.controller;

import com.bitokas.manager.dto.EstoqueDTO;
import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.service.EstoqueService;
import com.bitokas.manager.service.IngredienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;
    private final IngredienteService ingredienteService;

    @GetMapping
    public String listar(Model model) {
        List<EstoqueDTO> estoqueDTO = estoqueService.listarTodos();
        List<IngredienteDTO> ingredientesDTO = ingredienteService.listarTodos();

        model.addAttribute("ingredientes", ingredientesDTO);
        model.addAttribute("estoque", estoqueDTO);
        return "estoque/lista";
    }

    @GetMapping("/{ingredienteId}")
    public String buscarPorIngrediente(@PathVariable Long ingredienteId, Model model) {
        model.addAttribute("ingrediente", ingredienteService.buscarPorId(ingredienteId));
        model.addAttribute("itemEstoque", estoqueService.buscarPorIngrediente(ingredienteId));
        return "estoque/detalhe";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(@RequestParam Long ingredienteId,
                                   @RequestParam Double quantidade) {
        estoqueService.registrarEntrada(ingredienteId, quantidade);
        return "redirect:/estoque";
    }

    @PostMapping("/saida")
    public String registrarSaida(@RequestParam Long ingredienteId,
                                 @RequestParam Double quantidade) {
        estoqueService.registrarSaida(ingredienteId, quantidade);
        return "redirect:/estoque";
    }

    @PostMapping("/ajustar")
    public String ajustarEstoque(@RequestParam Long ingredienteId,
                                 @RequestParam Double quantidade) {
        estoqueService.ajustarEstoque(ingredienteId, quantidade);
        return "redirect:/estoque";
    }
}