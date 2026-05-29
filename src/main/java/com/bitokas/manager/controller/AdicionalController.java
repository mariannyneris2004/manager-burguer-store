package com.bitokas.manager.controller;

import com.bitokas.manager.dto.AdicionalDTO;
import com.bitokas.manager.service.AdicionalService;
import com.bitokas.manager.service.IngredienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/adicionais")
@RequiredArgsConstructor
public class AdicionalController {

    private final AdicionalService adicionalService;
    private final IngredienteService ingredienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("adicionais", adicionalService.listarTodos());
        return "adicionais/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("adicional", new AdicionalDTO());
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
        return "adicionais/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("adicional") AdicionalDTO adicionalDTO) {
        if (adicionalDTO.getId() != null){
            adicionalService.atualizar(adicionalDTO.getId(), adicionalDTO);
        } else {
            adicionalService.cadastrar(adicionalDTO);
        }
        return "redirect:/adicionais";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("adicional", adicionalService.buscarPorId(id));
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
        return "adicionais/form";
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        adicionalService.excluir(id);
        return "redirect:/adicionais";
    }

    @GetMapping("/{id}/ingredientes")
    public String listarIngredientesDoAdicional(@PathVariable Long id, Model model) {
        model.addAttribute("adicionalId", id);
        model.addAttribute("ingredientes", adicionalService.listarIngredientesDoAdicional(id));
        return "adicionais/ingredientes";
    }
}