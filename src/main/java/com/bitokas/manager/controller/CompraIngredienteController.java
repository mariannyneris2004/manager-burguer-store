package com.bitokas.manager.controller;

import com.bitokas.manager.dto.CompraIngredienteDTO;
import com.bitokas.manager.dto.CompraItemDTO;
import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.service.CompraIngredienteService;
import com.bitokas.manager.service.IngredienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
        CompraIngredienteDTO compraIngredienteDTO = new CompraIngredienteDTO();
        compraIngredienteDTO.setItens(new ArrayList<>());

        model.addAttribute("compra", compraIngredienteDTO);
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
        return "compras/form";
    }

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("compra") CompraIngredienteDTO compraDTO,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
            return "compras/form";
        }

        compraIngredienteService.registrarCompra(compraDTO);
        return "redirect:/compras";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        CompraIngredienteDTO compra = compraIngredienteService.buscarPorId(id);

        List<IngredienteDTO> ingredientes = new ArrayList<>();
        for (CompraItemDTO item : compra.getItens()) {
            ingredientes.add(ingredienteService.buscarPorId(item.getIngredienteId()));
        }

        model.addAttribute("compra", compra);
        model.addAttribute("ingredientes", ingredientes);
        return "compras/detalhe";
    }
}
