package com.bitokas.manager.service;

import com.bitokas.manager.dto.CompraIngredienteDTO;
import com.bitokas.manager.dto.CompraItemDTO;
import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.model.gastos.CompraIngrediente;
import com.bitokas.manager.model.gastos.CompraItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CompraIngredienteService {

    @PersistenceContext
    private EntityManager entityManager;

    private final EstoqueService estoqueService;
    private final IngredienteService ingredienteService;

    public CompraIngredienteDTO registrarCompra(CompraIngredienteDTO dto) {
        if (dto.getId() == null) {
            return criar(dto);
        }
        throw new UnsupportedOperationException("Edição de compra foi removida para evitar inconsistências no custo médio. Exclua e recadastre a compra se necessário.");
    }

    private CompraIngredienteDTO criar(CompraIngredienteDTO dto) {

        CompraIngrediente compra = new CompraIngrediente();

        compra.setDataCompra(
                dto.getDataCompra() == null ? new Date() : dto.getDataCompra()
        );

        compra.setValorTotal(calcularTotal(dto.getItens()));

        entityManager.persist(compra);
        entityManager.flush();

        salvarItens(compra.getId(), dto.getItens());
        aplicarEntradaNoEstoque(compra.getId(), dto.getItens());

        return buscarPorId(compra.getId());
    }

    private void aplicarEntradaNoEstoque(Long compraId, List<CompraItemDTO> itens) {
        if (itens == null) {
            return;
        }

        for (CompraItemDTO item : itens) {
            IngredienteDTO ingrediente = ingredienteService.buscarPorId(item.getIngredienteId());

            double quantidadeComprada = n(item.getQuantidade());
            double unidadesPorEmbalagem = n(item.getUnidadesPorEmbalagem());
            if (unidadesPorEmbalagem <= 0) {
                unidadesPorEmbalagem = n(ingrediente.getQuantidadePorUnidadeCompra());
            }
            if (unidadesPorEmbalagem <= 0) {
                unidadesPorEmbalagem = 1d;
            }

            double quantidadeEstoque = quantidadeComprada * unidadesPorEmbalagem;
            double custoUnitarioEstoque = quantidadeEstoque == 0 ? 0d : (quantidadeComprada * n(item.getValorUnitario())) / quantidadeEstoque;
            double valorTotalItem = quantidadeComprada * n(item.getValorUnitario());

            estoqueService.registrarEntradaCompra(
                    item.getIngredienteId(),
                    quantidadeEstoque,
                    custoUnitarioEstoque,
                    compraId
            );

            item.setUnidadesPorEmbalagem(unidadesPorEmbalagem);
            item.setQuantidadeEstoque(quantidadeEstoque);
            item.setCustoUnitarioEstoque(custoUnitarioEstoque);
            item.setValorTotalItem(valorTotalItem);
        }
    }

    public CompraIngredienteDTO buscarPorId(Long id) {
        CompraIngrediente compra = buscarEntidadePorId(id);
        return toDTOCompleto(compra);
    }

    public List<CompraIngredienteDTO> listarTodas() {
        return entityManager.createQuery(
                        "select c from CompraIngrediente c order by c.dataCompra desc",
                        CompraIngrediente.class)
                .getResultList()
                .stream()
                .map(this::toDTOCompleto)
                .toList();
    }

    private void salvarItens(Long compraId, List<CompraItemDTO> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("A compra precisa ter ao menos um item.");
        }

        for (CompraItemDTO itemDTO : itens) {
            if (itemDTO.getIngredienteId() == null) {
                continue;
            }

            IngredienteDTO ingrediente = ingredienteService.buscarPorId(itemDTO.getIngredienteId());

            double quantidadeComprada = n(itemDTO.getQuantidade());
            double unidadesPorEmbalagem = n(itemDTO.getUnidadesPorEmbalagem());
            if (unidadesPorEmbalagem <= 0) {
                unidadesPorEmbalagem = n(ingrediente.getQuantidadePorUnidadeCompra());
            }
            if (unidadesPorEmbalagem <= 0) {
                unidadesPorEmbalagem = 1d;
            }

            double quantidadeEstoque = quantidadeComprada * unidadesPorEmbalagem;
            double valorUnitario = n(itemDTO.getValorUnitario());
            double custoUnitarioEstoque = quantidadeEstoque == 0 ? 0d : (quantidadeComprada * valorUnitario) / quantidadeEstoque;
            double valorTotalItem = quantidadeComprada * valorUnitario;

            CompraItem item = new CompraItem();
            item.setCompraId(compraId);
            item.setIngredienteId(itemDTO.getIngredienteId());
            item.setQuantidade(quantidadeComprada);
            item.setUnidadesPorEmbalagem(unidadesPorEmbalagem);
            item.setQuantidadeEstoque(quantidadeEstoque);
            item.setValorUnitario(valorUnitario);
            item.setCustoUnitarioEstoque(custoUnitarioEstoque);
            item.setValorTotalItem(valorTotalItem);

            entityManager.persist(item);
        }
    }

    private List<CompraItemDTO> listarItensDaCompra(Long compraId) {
        return entityManager.createQuery(
                        "select ci from CompraItem ci where ci.compraId = :compraId",
                        CompraItem.class)
                .setParameter("compraId", compraId)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private CompraIngrediente buscarEntidadePorId(Long id) {
        CompraIngrediente compra = entityManager.find(CompraIngrediente.class, id);
        if (compra == null) {
            throw new IllegalArgumentException("Compra de ingrediente não encontrada: " + id);
        }
        return compra;
    }

    private CompraIngredienteDTO toDTOCompleto(CompraIngrediente compra) {
        return new CompraIngredienteDTO(
                compra.getId(),
                compra.getValorTotal(),
                compra.getDataCompra(),
                listarItensDaCompra(compra.getId())
        );
    }

    private CompraItemDTO toDTO(CompraItem item) {
        return new CompraItemDTO(
                item.getId(),
                item.getCompraId(),
                item.getIngredienteId(),
                item.getQuantidade(),
                item.getUnidadesPorEmbalagem(),
                item.getQuantidadeEstoque(),
                item.getValorUnitario(),
                item.getCustoUnitarioEstoque(),
                item.getValorTotalItem()
        );
    }

    private double calcularTotal(List<CompraItemDTO> itens) {
        if (itens == null) {
            return 0d;
        }
        double total = 0d;
        for (CompraItemDTO item : itens) {
            total += n(item.getQuantidade()) * n(item.getValorUnitario());
        }
        return total;
    }

    private double n(Double valor) {
        return valor == null ? 0d : valor;
    }
}
