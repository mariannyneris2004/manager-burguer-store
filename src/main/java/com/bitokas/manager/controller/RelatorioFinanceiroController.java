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
    public String gerarRelatorio(
            @RequestParam("inicio")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime inicio,

            @RequestParam("fim")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fim,

            Model model
    ) {
        if (inicio == null || fim == null) {
            model.addAttribute("erro", "Datas inválidas!");
            model.addAttribute("relatorio", new RelatorioFinanceiroDTO());
            return "relatorios/financeiro";
        }

        if (fim.isBefore(inicio)) {
            model.addAttribute("erro", "A data final não pode ser antes da inicial.");
            model.addAttribute("relatorio", new RelatorioFinanceiroDTO(inicio, fim));
            return "relatorios/financeiro";
        }

        RelatorioFinanceiroDTO resumo = relatorioFinanceiroService.gerarResumoFinanceiro(inicio, fim);

        model.addAttribute("relatorio", resumo);
        model.addAttribute("pedidos", relatorioFinanceiroService.listarPedidosPeriodo(inicio, fim));

        return "relatorios/financeiro";
    }
}
