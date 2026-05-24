package com.bitokas.manager.controller;

import com.bitokas.manager.dto.CompraIngredienteDTO;
import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.service.CompraIngredienteService;
import com.bitokas.manager.service.IngredienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraIngredienteController {

    private final CompraIngredienteService compraIngredienteService;
    private final IngredienteService ingredienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("compras", compraIngredienteService.listarTodas());
        return "compras/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("compra", new CompraIngredienteDTO());
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
        return "compras/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("compra") CompraIngredienteDTO compraDTO) {
        compraIngredienteService.registrarCompra(compraDTO);
        return "redirect:/compras";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("compra", compraIngredienteService.buscarPorId(id));
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
        return "compras/form";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("compra", compraIngredienteService.buscarPorId(id));
        return "compras/detalhe";
    }

    @PostMapping("/{id}/recalcular")
    public String recalcular(@PathVariable Long id) {
        compraIngredienteService.recalcularValorTotal(id);
        return "redirect:/compras/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        compraIngredienteService.excluir(id);
        return "redirect:/compras";
    }
}