package com.bitokas.manager.controller;

import com.bitokas.manager.dto.AdicionalDTO;
import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.dto.ProdutoDTO;
import com.bitokas.manager.service.AdicionalService;
import com.bitokas.manager.service.IngredienteService;
import com.bitokas.manager.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;
    private final IngredienteService ingredienteService;
    private final AdicionalService adicionalService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        return "produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new ProdutoDTO());
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
        model.addAttribute("adicionaisDisponiveis", adicionalService.listarTodos());
        return "produtos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("produto") ProdutoDTO produtoDTO) {
        produtoService.cadastrar(produtoDTO);
        return "redirect:/produtos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("produto", produtoService.buscarPorId(id));
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
        model.addAttribute("adicionaisDisponiveis", adicionalService.listarTodos());
        return "produtos/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return "redirect:/produtos";
    }

    @GetMapping("/categoria/{categoria}")
    public String listarPorCategoria(@PathVariable String categoria, Model model) {
        model.addAttribute("produtos", produtoService.listarPorCategoria(categoria));
        model.addAttribute("categoria", categoria);
        return "produtos/lista";
    }

    @GetMapping("/{id}/ingredientes")
    public String listarIngredientesDoProduto(@PathVariable Long id, Model model) {
        model.addAttribute("produtoId", id);
        model.addAttribute("ingredientes", produtoService.listarIngredientesDoProduto(id));
        return "produtos/ingredientes";
    }

    @GetMapping("/{id}/adicionais")
    public String listarAdicionaisPermitidos(@PathVariable Long id, Model model) {
        model.addAttribute("produtoId", id);
        model.addAttribute("adicionais", produtoService.listarAdicionaisPermitidos(id));
        return "produtos/adicionais";
    }
}