package com.bitokas.manager.controller;

import com.bitokas.manager.dto.RelatorioFinanceiroDTO;
import com.bitokas.manager.service.RelatorioFinanceiroService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioFinanceiroController {

    private final RelatorioFinanceiroService relatorioFinanceiroService;

    @GetMapping("/financeiro")
    public String financeiro(Model model) {
        model.addAttribute("relatorio", new RelatorioFinanceiroDTO());
        return "relatorios/financeiro";
    }

    @PostMapping("/financeiro")
    public String gerarFinanceiro(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
                                  Model model) {

        model.addAttribute("relatorio", relatorioFinanceiroService.gerarResumoFinanceiro(inicio, fim));
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        return "relatorios/financeiro";
    }
}