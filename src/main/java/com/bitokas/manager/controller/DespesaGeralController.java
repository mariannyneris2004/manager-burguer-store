package com.bitokas.manager.controller;

import com.bitokas.manager.dto.DespesaGeralDTO;
import com.bitokas.manager.model.gastos.Frequencia;
import com.bitokas.manager.service.DespesaGeralService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/despesas")
@RequiredArgsConstructor
public class DespesaGeralController {

    private final DespesaGeralService despesaGeralService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("despesas", despesaGeralService.listarTodas());
        model.addAttribute("frequencias", Frequencia.values());
        return "despesas/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("despesa", new DespesaGeralDTO());
        model.addAttribute("frequencias", Frequencia.values());
        return "despesas/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("despesa") DespesaGeralDTO despesaDTO) {
        if (despesaDTO.getId() != null){
            despesaGeralService.atualizar(despesaDTO.getId(), despesaDTO);
        } else {
            despesaGeralService.cadastrar(despesaDTO);
        }
        return "redirect:/despesas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("despesa", despesaGeralService.buscarPorId(id));
        model.addAttribute("frequencias", Frequencia.values());
        return "despesas/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        despesaGeralService.excluir(id);
        return "redirect:/despesas";
    }

    @GetMapping("/frequencia/{frequencia}")
    public String listarPorFrequencia(@PathVariable Frequencia frequencia, Model model) {
        model.addAttribute("despesas", despesaGeralService.listarPorFrequencia(frequencia));
        model.addAttribute("frequenciaSelecionada", frequencia);
        model.addAttribute("frequencias", Frequencia.values());
        return "despesas/lista";
    }

    @GetMapping("/periodo")
    public String listarPorPeriodo(
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date fim,
            Model model) {

        if (inicio != null && fim == null){
            fim = new Date();
        } else if (inicio == null && fim != null){
            inicio = fim;
        }

        model.addAttribute("despesas", despesaGeralService.listarPorPeriodo(inicio, fim));
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        model.addAttribute("frequencias", Frequencia.values());
        return "despesas/lista";
    }
}