package com.bitokas.manager.controller;

import com.bitokas.manager.dto.PedidoDTO;
import com.bitokas.manager.model.pedidos.TipoEntrega;
import com.bitokas.manager.service.AdicionalService;
import com.bitokas.manager.service.IngredienteService;
import com.bitokas.manager.service.PedidoService;
import com.bitokas.manager.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;
    private final AdicionalService adicionalService;
    private final IngredienteService ingredienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        return "pedidos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormulario(model, new PedidoDTO());
        return "pedidos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("pedido") PedidoDTO pedidoDTO) {
        if (pedidoDTO.getId() != null) {
            pedidoService.atualizar(pedidoDTO.getId(), pedidoDTO);
        } else {
            pedidoService.registrarPedido(pedidoDTO);
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        prepararFormulario(model, pedidoService.buscarPorId(id));
        return "pedidos/form";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.buscarPorId(id));
        return "pedidos/detalhe";
    }

    @PostMapping("/calcular-total")
    public String calcularTotal(@ModelAttribute("pedido") PedidoDTO pedidoDTO, Model model) {
        model.addAttribute("totalCalculado", pedidoService.calcularValorTotal(pedidoDTO));
        prepararFormulario(model, pedidoDTO);
        return "pedidos/form";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        pedidoService.excluir(id);
        return "redirect:/pedidos";
    }

    private void prepararFormulario(Model model, PedidoDTO pedido) {
        if (pedido.getItens() == null) {
            pedido.setItens(new java.util.ArrayList<>());
        }
        model.addAttribute("pedido", pedido);
        model.addAttribute("tiposEntrega", TipoEntrega.values());
        model.addAttribute("produtosDisponiveis", produtoService.listarTodos());
        model.addAttribute("adicionaisDisponiveis", adicionalService.listarTodos());
        model.addAttribute("ingredientesDisponiveis", ingredienteService.listarTodos());
    }
}
