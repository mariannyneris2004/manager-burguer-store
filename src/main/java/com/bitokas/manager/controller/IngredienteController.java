package com.bitokas.manager.controller;

import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.service.IngredienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ingredientes")
@RequiredArgsConstructor
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ingredientes", ingredienteService.listarTodos());
        return "ingredientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("ingrediente", new IngredienteDTO());
        return "ingredientes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("ingrediente") IngredienteDTO ingredienteDTO) {
        if (ingredienteDTO.getId() != null) {
            ingredienteService.atualizar(ingredienteDTO.getId(), ingredienteDTO);
        } else {
            ingredienteService.cadastrar(ingredienteDTO);
        }
        return "redirect:/ingredientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("ingrediente", ingredienteService.buscarPorId(id));
        return "ingredientes/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        ingredienteService.excluir(id);
        return "redirect:/ingredientes";
    }

    @GetMapping("/buscar")
    public String buscarPorNome(@RequestParam String nome, Model model) {
        model.addAttribute("ingredientes", ingredienteService.buscarPorNome(nome));
        return "ingredientes/lista";
    }
}