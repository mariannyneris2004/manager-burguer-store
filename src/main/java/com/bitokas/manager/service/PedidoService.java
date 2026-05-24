package com.bitokas.manager.service;

import com.bitokas.manager.dto.PedidoAdicionalDTO;
import com.bitokas.manager.dto.PedidoDTO;
import com.bitokas.manager.dto.PedidoProdutoDTO;
import com.bitokas.manager.model.pedidos.Pedido;
import com.bitokas.manager.model.pedidos.PedidoAdicional;
import com.bitokas.manager.model.pedidos.PedidoProduto;
import com.bitokas.manager.model.pedidos.TipoEntrega;
import com.bitokas.manager.model.produtos.Adicional;
import com.bitokas.manager.model.produtos.Produto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PedidoService {

    @PersistenceContext
    private EntityManager entityManager;

    private final EstoqueService estoqueService;

    public PedidoDTO registrarPedido(PedidoDTO dto) {
        if (dto.getProdutos() == null || dto.getProdutos().isEmpty()) {
            throw new IllegalArgumentException("O pedido precisa ter ao menos um produto.");
        }

        dto.setProdutos(
                dto.getProdutos().stream()
                        .filter(p -> p.getProdutoId() != null)
                        .toList()
        );

        dto.setAdicionais(
                dto.getAdicionais() == null ? null :
                        dto.getAdicionais().stream()
                        .filter(a -> a.getAdicionalId() != null)
                        .toList()
        );

        Pedido pedido = new Pedido();
        pedido.setNomeCliente(dto.getNomeCliente());
        pedido.setDataHora(dto.getDataHora() == null ? LocalDateTime.now() : dto.getDataHora());
        pedido.setTipoEntrega(dto.getTipoEntrega());
        pedido.setValorEntrega(dto.getValorEntrega());

        double valorTotal = calcularValorTotal(dto);
        pedido.setValorTotal(valorTotal);
        pedido.setValorPago(dto.getValorPago() == null ? valorTotal : dto.getValorPago());

        entityManager.persist(pedido);
        entityManager.flush();

        salvarProdutosDoPedido(pedido.getId(), dto.getProdutos());
        salvarAdicionaisDoPedido(pedido.getId(), dto.getAdicionais());

        baixarEstoqueDoPedido(pedido.getId());

        return buscarPorId(pedido.getId());
    }

    public PedidoDTO buscarPorId(Long id) {
        Pedido pedido = buscarEntidadePorId(id);
        return toDTOCompleto(pedido);
    }

    public List<PedidoDTO> listarTodos() {
        return entityManager.createQuery("select p from Pedido p order by p.dataHora desc", Pedido.class)
                .getResultList()
                .stream()
                .map(this::toDTOCompleto)
                .toList();
    }

    public List<PedidoDTO> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return entityManager.createQuery(
                        "select p from Pedido p where p.dataHora between :inicio and :fim order by p.dataHora desc",
                        Pedido.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList()
                .stream()
                .map(this::toDTOCompleto)
                .toList();
    }

    public Double calcularValorTotal(PedidoDTO dto) {
        double total = 0d;

        if (dto.getProdutos() != null) {
            for (PedidoProdutoDTO item : dto.getProdutos()) {
                Produto produto = entityManager.find(Produto.class, item.getProdutoId());
                if (produto == null) {
                    throw new IllegalArgumentException("Produto não encontrado: " + item.getProdutoId());
                }
                total += n(produto.getValorVenda()) * n(item.getQuantidade());
            }
        }

        if (dto.getAdicionais() != null) {
            for (PedidoAdicionalDTO item : dto.getAdicionais()) {
                Adicional adicional = entityManager.find(Adicional.class, item.getAdicionalId());
                if (adicional == null) {
                    throw new IllegalArgumentException("Adicional não encontrado: " + item.getAdicionalId());
                }
                total += n(adicional.getValorBase()) * n(item.getQuantidade());
            }
        }

        if (dto.getTipoEntrega() == TipoEntrega.ENTREGA) {
            total += n(dto.getValorEntrega());
        }

        return total;
    }

    public void cancelarPedido(Long id) {
        Pedido pedido = buscarEntidadePorId(id);

        List<PedidoProduto> produtos = listarProdutosEntidadeDoPedido(id);
        List<PedidoAdicional> adicionais = listarAdicionaisEntidadeDoPedido(id);

        for (PedidoProduto item : produtos) {
            estoqueService.registrarEntradaPorProduto(item.getProdutoId(), item.getQuantidade());
        }

        for (PedidoAdicional item : adicionais) {
            estoqueService.registrarEntradaPorAdicional(item.getAdicionalId(), item.getQuantidade());
        }

        entityManager.createQuery("delete from PedidoProduto pp where pp.pedidoId = :pedidoId")
                .setParameter("pedidoId", id)
                .executeUpdate();

        entityManager.createQuery("delete from PedidoAdicional pa where pa.pedidoId = :pedidoId")
                .setParameter("pedidoId", id)
                .executeUpdate();

        entityManager.remove(entityManager.contains(pedido) ? pedido : entityManager.merge(pedido));
    }

    public void baixarEstoqueDoPedido(Long pedidoId) {
        List<PedidoProduto> produtos = listarProdutosEntidadeDoPedido(pedidoId);
        List<PedidoAdicional> adicionais = listarAdicionaisEntidadeDoPedido(pedidoId);

        for (PedidoProduto item : produtos) {
            estoqueService.baixarIngredientesDoProduto(item.getProdutoId(), item.getQuantidade());
        }

        for (PedidoAdicional item : adicionais) {
            estoqueService.baixarIngredientesDoAdicional(item.getAdicionalId(), item.getQuantidade());
        }
    }

    private void salvarProdutosDoPedido(Long pedidoId, List<PedidoProdutoDTO> produtos) {
        if (produtos == null) {
            return;
        }

        for (PedidoProdutoDTO itemDTO : produtos) {
            PedidoProduto item = new PedidoProduto();
            item.setPedidoId(pedidoId);
            item.setProdutoId(itemDTO.getProdutoId());
            item.setQuantidade(itemDTO.getQuantidade());
            entityManager.persist(item);
        }
    }

    private void salvarAdicionaisDoPedido(Long pedidoId, List<PedidoAdicionalDTO> adicionais) {
        if (adicionais == null) {
            return;
        }

        for (PedidoAdicionalDTO itemDTO : adicionais) {
            PedidoAdicional item = new PedidoAdicional();
            item.setPedidoId(pedidoId);
            item.setAdicionalId(itemDTO.getAdicionalId());
            item.setQuantidade(itemDTO.getQuantidade());
            entityManager.persist(item);
        }
    }

    private List<PedidoProduto> listarProdutosEntidadeDoPedido(Long pedidoId) {
        return entityManager.createQuery(
                        "select pp from PedidoProduto pp where pp.pedidoId = :pedidoId",
                        PedidoProduto.class)
                .setParameter("pedidoId", pedidoId)
                .getResultList();
    }

    private List<PedidoAdicional> listarAdicionaisEntidadeDoPedido(Long pedidoId) {
        return entityManager.createQuery(
                        "select pa from PedidoAdicional pa where pa.pedidoId = :pedidoId",
                        PedidoAdicional.class)
                .setParameter("pedidoId", pedidoId)
                .getResultList();
    }

    private Pedido buscarEntidadePorId(Long id) {
        Pedido pedido = entityManager.find(Pedido.class, id);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não encontrado: " + id);
        }
        return pedido;
    }

    private PedidoDTO toDTOCompleto(Pedido pedido) {
        return new PedidoDTO(
                pedido.getId(),
                pedido.getNomeCliente(),
                pedido.getDataHora(),
                pedido.getTipoEntrega(),
                pedido.getValorEntrega(),
                pedido.getValorTotal(),
                pedido.getValorPago(),
                listarProdutosDoPedido(pedido.getId()),
                listarAdicionaisDoPedido(pedido.getId())
        );
    }

    private List<PedidoProdutoDTO> listarProdutosDoPedido(Long pedidoId) {
        return listarProdutosEntidadeDoPedido(pedidoId).stream()
                .map(item -> new PedidoProdutoDTO(
                        item.getId(),
                        item.getPedidoId(),
                        item.getProdutoId(),
                        item.getQuantidade()
                ))
                .toList();
    }

    private List<PedidoAdicionalDTO> listarAdicionaisDoPedido(Long pedidoId) {
        return listarAdicionaisEntidadeDoPedido(pedidoId).stream()
                .map(item -> new PedidoAdicionalDTO(
                        item.getId(),
                        item.getPedidoId(),
                        item.getAdicionalId(),
                        item.getQuantidade()
                ))
                .toList();
    }

    private double n(Double valor) {
        return valor == null ? 0d : valor;
    }

    private Integer n(Integer valor) {
        return valor == null ? 0 : valor;
    }
}