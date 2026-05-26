package com.bitokas.manager.service;

import com.bitokas.manager.dto.EstoqueDTO;
import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.model.gastos.Estoque;
import com.bitokas.manager.model.gastos.CompraItem;
import com.bitokas.manager.model.produtos.AdicionalIngrediente;
import com.bitokas.manager.model.produtos.ProdutoIngrediente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EstoqueService {

    @PersistenceContext
    private EntityManager entityManager;

    private final IngredienteService ingredienteService;


    public EstoqueDTO registrarEntrada(Long ingredienteId, Double quantidade) {
        Estoque estoque = buscarEntidadePorIngrediente(ingredienteId);

        if (estoque == null) {
            estoque = new Estoque();
            estoque.setIngredienteId(ingredienteId);
            estoque.setQuantidade(0d);
            entityManager.persist(estoque);
        }

        estoque.setQuantidade(n(estoque.getQuantidade()) + n(quantidade));
        entityManager.merge(estoque);

        return toDTO(estoque);
    }

    public EstoqueDTO registrarSaida(Long ingredienteId, Double quantidade) {
        Estoque estoque = buscarOuFalharPorIngrediente(ingredienteId);
        double saldoNovo = n(estoque.getQuantidade()) - n(quantidade);

        if (saldoNovo < 0) {
            throw new IllegalStateException("Estoque insuficiente para o ingrediente " + ingredienteId);
        }

        estoque.setQuantidade(saldoNovo);
        entityManager.merge(estoque);
        return toDTO(estoque);
    }

    public EstoqueDTO ajustarEstoque(Long ingredienteId, Double quantidade) {
        if (quantidade == null) {
            quantidade = 0d;
        }

        if (quantidade >= 0) {
            return registrarEntrada(ingredienteId, quantidade);
        }

        return registrarSaida(ingredienteId, Math.abs(quantidade));
    }

    public EstoqueDTO buscarPorIngrediente(Long ingredienteId) {
        Estoque estoque = buscarEntidadePorIngrediente(ingredienteId);
        if (estoque == null) {
            throw new IllegalArgumentException("Não existe estoque cadastrado para o ingrediente " + ingredienteId);
        }
        return toDTO(estoque);
    }

    public List<EstoqueDTO> listarTodos() {
        List<IngredienteDTO> ingredientes = ingredienteService.listarTodos();
        List<EstoqueDTO> estoqueDTOList = new ArrayList<>();

        for (IngredienteDTO ingrediente : ingredientes) {

            Estoque estoque = buscarEntidadePorIngrediente(ingrediente.getId());

            if (estoque == null) {

                estoqueDTOList.add(
                        new EstoqueDTO(
                                null,
                                ingrediente.getId(),
                                0d
                        )
                );

            } else {

                estoqueDTOList.add(toDTO(estoque));
            }
        }

        return estoqueDTOList;
    }

    public void baixarIngredientesDoProduto(Long produtoId, Integer quantidadeProduto) {
        List<ProdutoIngrediente> itens = entityManager.createQuery(
                        "select pi from ProdutoIngrediente pi where pi.produtoId = :produtoId",
                        ProdutoIngrediente.class)
                .setParameter("produtoId", produtoId)
                .getResultList();

        int multiplicador = quantidadeProduto == null ? 1 : quantidadeProduto;

        for (ProdutoIngrediente item : itens) {
            registrarSaida(item.getIngredienteId(), n(item.getQuantidade()) * multiplicador);
        }
    }

    public void baixarIngredientesDoAdicional(Long adicionalId, Integer quantidadeAdicional) {
        List<AdicionalIngrediente> itens = entityManager.createQuery(
                        "select ai from AdicionalIngrediente ai where ai.adicionalId = :adicionalId",
                        AdicionalIngrediente.class)
                .setParameter("adicionalId", adicionalId)
                .getResultList();

        int multiplicador = quantidadeAdicional == null ? 1 : quantidadeAdicional;

        for (AdicionalIngrediente item : itens) {
            registrarSaida(item.getIngredienteId(), n(item.getQuantidade()) * multiplicador);
        }
    }

    public void reporEstoquePelaCompra(Long compraId) {
        List<CompraItem> itens = entityManager.createQuery(
                        "select ci from CompraItem ci where ci.compraId = :compraId",
                        CompraItem.class)
                .setParameter("compraId", compraId)
                .getResultList();

        for (CompraItem item : itens) {
            registrarEntrada(item.getIngredienteId(), n(item.getQuantidade()));
        }
    }

    private Estoque buscarEntidadePorIngrediente(Long ingredienteId) {
        return entityManager.createQuery(
                        "select e from Estoque e where e.ingredienteId = :ingredienteId",
                        Estoque.class)
                .setParameter("ingredienteId", ingredienteId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private Estoque buscarOuFalharPorIngrediente(Long ingredienteId) {
        Estoque estoque = buscarEntidadePorIngrediente(ingredienteId);
        if (estoque == null) {
            throw new IllegalArgumentException("Estoque não encontrado para o ingrediente " + ingredienteId);
        }
        return estoque;
    }

    private EstoqueDTO toDTO(Estoque estoque) {
        return new EstoqueDTO(
                estoque.getId(),
                estoque.getIngredienteId(),
                estoque.getQuantidade()
        );
    }

    private double n(Double valor) {
        return valor == null ? 0d : valor;
    }

    public void registrarEntradaPorProduto(Long produtoId, Integer quantidadeProduto) {
        List<ProdutoIngrediente> itens = entityManager.createQuery(
                        "select pi from ProdutoIngrediente pi where pi.produtoId = :produtoId",
                        ProdutoIngrediente.class)
                .setParameter("produtoId", produtoId)
                .getResultList();

        int multiplicador = quantidadeProduto == null ? 1 : quantidadeProduto;

        for (ProdutoIngrediente item : itens) {
            registrarEntrada(item.getIngredienteId(), n(item.getQuantidade()) * multiplicador);
        }
    }

    public void registrarEntradaPorAdicional(Long adicionalId, Integer quantidadeAdicional) {
        List<AdicionalIngrediente> itens = entityManager.createQuery(
                        "select ai from AdicionalIngrediente ai where ai.adicionalId = :adicionalId",
                        AdicionalIngrediente.class)
                .setParameter("adicionalId", adicionalId)
                .getResultList();

        int multiplicador = quantidadeAdicional == null ? 1 : quantidadeAdicional;

        for (AdicionalIngrediente item : itens) {
            registrarEntrada(item.getIngredienteId(), n(item.getQuantidade()) * multiplicador);
        }
    }
}