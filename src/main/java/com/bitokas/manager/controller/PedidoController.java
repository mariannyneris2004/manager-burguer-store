package com.bitokas.manager.controller;

import com.bitokas.manager.dto.PedidoDTO;
import com.bitokas.manager.model.pedidos.TipoEntrega;
import com.bitokas.manager.service.AdicionalService;
import com.bitokas.manager.service.PedidoService;
import com.bitokas.manager.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;
    private final AdicionalService adicionalService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        return "pedidos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pedido", new PedidoDTO());
        model.addAttribute("tiposEntrega", TipoEntrega.values());
        model.addAttribute("produtosDisponiveis", produtoService.listarTodos());
        model.addAttribute("adicionaisDisponiveis", adicionalService.listarTodos());
        return "pedidos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("pedido") PedidoDTO pedidoDTO) {
        pedidoService.registrarPedido(pedidoDTO);
        return "redirect:/pedidos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.buscarPorId(id));
        model.addAttribute("tiposEntrega", TipoEntrega.values());
        model.addAttribute("produtosDisponiveis", produtoService.listarTodos());
        model.addAttribute("adicionaisDisponiveis", adicionalService.listarTodos());
        return "pedidos/form";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.buscarPorId(id));
        return "lista";
    }

    @GetMapping("/periodo")
    public String listarPorPeriodo(@RequestParam LocalDateTime inicio,
                                   @RequestParam LocalDateTime fim,
                                   Model model) {
        model.addAttribute("pedidos", pedidoService.listarPorPeriodo(inicio, fim));
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        return "pedidos/lista";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return "redirect:/pedidos";
    }

    @PostMapping("/calcular-total")
    public String calcularTotal(@ModelAttribute("pedido") PedidoDTO pedidoDTO, Model model) {
        model.addAttribute("totalCalculado", pedidoService.calcularValorTotal(pedidoDTO));
        model.addAttribute("pedido", pedidoDTO);
        model.addAttribute("tiposEntrega", TipoEntrega.values());
        model.addAttribute("produtosDisponiveis", produtoService.listarTodos());
        model.addAttribute("adicionaisDisponiveis", adicionalService.listarTodos());
        return "pedidos/form";
    }
}