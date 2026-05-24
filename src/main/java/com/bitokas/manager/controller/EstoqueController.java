package com.bitokas.manager.controller;

import com.bitokas.manager.dto.EstoqueDTO;
import com.bitokas.manager.service.EstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estoque", estoqueService.listarTodos());
        return "estoque/lista";
    }

    @GetMapping("/{ingredienteId}")
    public String buscarPorIngrediente(@PathVariable Long ingredienteId, Model model) {
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