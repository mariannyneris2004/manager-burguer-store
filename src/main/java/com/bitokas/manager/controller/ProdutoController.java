package com.bitokas.manager.controller;

import com.bitokas.manager.dto.*;
import com.bitokas.manager.service.AdicionalService;
import com.bitokas.manager.service.IngredienteService;
import com.bitokas.manager.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
        ProdutoDTO dto = new ProdutoDTO();

        List<ProdutoIngredienteDTO> ingredientes = ingredienteService
                .listarTodos()
                .stream()
                .map(ing -> {
                    ProdutoIngredienteDTO pi = new ProdutoIngredienteDTO();
                    pi.setIngredienteId(ing.getId());
                    pi.setQuantidade(0.0);
                    pi.setSelecionado(false);
                    return pi;
                })
                .toList();

        dto.setIngredientes(new ArrayList<>(ingredientes));

        List<ProdutoAdicionalDTO> adicionais = adicionalService
                .listarTodos()
                .stream()
                .map(adc -> {
                    ProdutoAdicionalDTO pa = new ProdutoAdicionalDTO();
                    pa.setAdicionalId(adc.getId());
                    pa.setSelecionado(false);
                    return pa;
                })
                .toList();

        dto.setAdicionaisPermitidos(new ArrayList<>(adicionais));

        model.addAttribute("produto", dto);
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
        model.addAttribute("adicionaisDisponiveis", adicionalService.listarTodos());

        return "produtos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("produto") ProdutoDTO produtoDTO) {
        if (produtoDTO.getId() != null) {
            produtoService.atualizar(produtoDTO.getId(), produtoDTO);
        } else {
            produtoService.cadastrar(produtoDTO);
        }

        return "redirect:/produtos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        ProdutoDTO dto = produtoService.buscarPorId(id);
        List<IngredienteDTO> disponiveis = ingredienteService.listarTodos();

        List<ProdutoIngredienteDTO> ingredientesTela = new ArrayList<>();
        for (IngredienteDTO ingrediente : disponiveis) {
            ProdutoIngredienteDTO existente =
                    dto.getIngredientes().stream()
                            .filter(i -> i.getIngredienteId().equals(ingrediente.getId()))
                            .findFirst()
                            .orElse(null);

            ProdutoIngredienteDTO item = new ProdutoIngredienteDTO();
            item.setIngredienteId(ingrediente.getId());

            if (existente != null) {
                item.setId(existente.getId());
                item.setProdutoId(existente.getProdutoId());
                item.setQuantidade(existente.getQuantidade());
                item.setSelecionado(true);
            } else {
                item.setQuantidade(0.0);
                item.setSelecionado(false);
            }

            ingredientesTela.add(item);
        }
        dto.setIngredientes(ingredientesTela);

        List<ProdutoAdicionalDTO> adicionaisTela = new ArrayList<>();
        for (AdicionalDTO adicional : adicionalService.listarTodos()) {
            ProdutoAdicionalDTO existente =
                    dto.getAdicionaisPermitidos().stream()
                            .filter(a -> a.getAdicionalId().equals(adicional.getId()))
                            .findFirst()
                            .orElse(null);

            ProdutoAdicionalDTO item = new ProdutoAdicionalDTO();
            item.setAdicionalId(adicional.getId());

            if (existente != null) {
                item.setId(existente.getId());
                item.setProdutoId(existente.getProdutoId());
                item.setSelecionado(true);
            } else {
                item.setSelecionado(false);
            }

            adicionaisTela.add(item);
        }
        dto.setAdicionaisPermitidos(adicionaisTela);

        model.addAttribute("produto", dto);
        model.addAttribute("ingredientesDisponiveis", disponiveis);
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
