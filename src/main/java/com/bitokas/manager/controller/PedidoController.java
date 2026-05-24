package com.bitokas.manager.controller;

import com.bitokas.manager.dto.PedidoAdicionalDTO;
import com.bitokas.manager.dto.PedidoDTO;
import com.bitokas.manager.dto.PedidoProdutoDTO;
import com.bitokas.manager.model.pedidos.TipoEntrega;
import com.bitokas.manager.service.AdicionalService;
import com.bitokas.manager.service.PedidoService;
import com.bitokas.manager.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        PedidoDTO dto = new PedidoDTO();

        dto.setProdutos(
                produtoService.listarTodos()
                        .stream()
                        .map(p -> {
                            PedidoProdutoDTO item = new PedidoProdutoDTO();

                            item.setProdutoId(p.getId());
                            item.setQuantidade(0);
                            item.setSelecionado(false);

                            return item;
                        })
                        .toList()
        );

        dto.setAdicionais(
                adicionalService.listarTodos()
                        .stream()
                        .map(a -> {
                            PedidoAdicionalDTO item = new PedidoAdicionalDTO();

                            item.setAdicionalId(a.getId());
                            item.setQuantidade(0);
                            item.setSelecionado(false);

                            return item;
                        })
                        .toList()
        );

        model.addAttribute("pedido", dto);
        model.addAttribute("tiposEntrega", TipoEntrega.values());
        model.addAttribute("produtosDisponiveis", produtoService.listarTodos());
        model.addAttribute("adicionaisDisponiveis", adicionalService.listarTodos());

        return "pedidos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("pedido") PedidoDTO pedidoDTO) {
        if (pedidoDTO.getId() != null){
            pedidoService.atualizar(pedidoDTO.getId(), pedidoDTO);
        } else {
            pedidoService.registrarPedido(pedidoDTO);
        }
        return "redirect:/pedidos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        PedidoDTO dto = pedidoService.buscarPorId(id);

        List<PedidoProdutoDTO> produtosTela = new ArrayList<>();

        produtoService.listarTodos().forEach(produto -> {

            PedidoProdutoDTO existente =
                    dto.getProdutos()
                            .stream()
                            .filter(p -> p.getProdutoId().equals(produto.getId()))
                            .findFirst()
                            .orElse(null);

            PedidoProdutoDTO item = new PedidoProdutoDTO();

            item.setProdutoId(produto.getId());

            if (existente != null) {
                item.setId(existente.getId());
                item.setPedidoId(existente.getPedidoId());
                item.setQuantidade(existente.getQuantidade());
                item.setSelecionado(true);
            } else {
                item.setQuantidade(0);
                item.setSelecionado(false);
            }

            produtosTela.add(item);
        });

        dto.setProdutos(produtosTela);

        List<PedidoAdicionalDTO> adicionaisTela = new ArrayList<>();

        adicionalService.listarTodos().forEach(adicional -> {

            PedidoAdicionalDTO existente =
                    dto.getAdicionais()
                            .stream()
                            .filter(a -> a.getAdicionalId().equals(adicional.getId()))
                            .findFirst()
                            .orElse(null);

            PedidoAdicionalDTO item = new PedidoAdicionalDTO();

            item.setAdicionalId(adicional.getId());

            if (existente != null) {
                item.setId(existente.getId());
                item.setPedidoId(existente.getPedidoId());
                item.setQuantidade(existente.getQuantidade());
                item.setSelecionado(true);
            } else {
                item.setQuantidade(0);
                item.setSelecionado(false);
            }

            adicionaisTela.add(item);
        });

        dto.setAdicionais(adicionaisTela);

        model.addAttribute("pedido", dto);
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

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        pedidoService.excluir(id);
        return "redirect:/pedidos";
    }
}