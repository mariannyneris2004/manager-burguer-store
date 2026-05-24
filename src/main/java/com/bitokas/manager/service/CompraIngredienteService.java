package com.bitokas.manager.service;

import com.bitokas.manager.dto.CompraIngredienteDTO;
import com.bitokas.manager.dto.CompraItemDTO;
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

    public CompraIngredienteDTO registrarCompra(CompraIngredienteDTO dto) {
        CompraIngrediente compra = new CompraIngrediente();
        compra.setDataCompra(dto.getDataCompra() == null ? new Date() : dto.getDataCompra());

        double totalCalculado = calcularTotal(dto.getItens());
        compra.setValorTotal(dto.getValorTotal() == null ? totalCalculado : dto.getValorTotal());

        entityManager.persist(compra);
        entityManager.flush();

        salvarItens(compra.getId(), dto.getItens());
        if (dto.getItens() != null) {
            for (CompraItemDTO item : dto.getItens()) {
                estoqueService.registrarEntrada(item.getIngredienteId(), item.getQuantidade());
            }
        }

        return buscarPorId(compra.getId());
    }

    public CompraIngredienteDTO buscarPorId(Long id) {
        CompraIngrediente compra = buscarEntidadePorId(id);
        return toDTOCompleto(compra);
    }

    public List<CompraIngredienteDTO> listarTodas() {
        return entityManager.createQuery("select c from CompraIngrediente c order by c.dataCompra desc", CompraIngrediente.class)
                .getResultList()
                .stream()
                .map(this::toDTOCompleto)
                .toList();
    }

    public CompraIngredienteDTO recalcularValorTotal(Long compraId) {
        CompraIngrediente compra = buscarEntidadePorId(compraId);
        List<CompraItemDTO> itens = listarItensDaCompra(compraId);

        double total = calcularTotal(itens);
        compra.setValorTotal(total);
        entityManager.merge(compra);

        return buscarPorId(compraId);
    }

    public void excluir(Long id) {
        List<CompraItem> itens = entityManager.createQuery(
                        "select ci from CompraItem ci where ci.compraId = :compraId",
                        CompraItem.class)
                .setParameter("compraId", id)
                .getResultList();

        for (CompraItem item : itens) {
            estoqueService.registrarSaida(item.getIngredienteId(), item.getQuantidade());
        }

        entityManager.createQuery("delete from CompraItem ci where ci.compraId = :compraId")
                .setParameter("compraId", id)
                .executeUpdate();

        CompraIngrediente compra = buscarEntidadePorId(id);
        entityManager.remove(entityManager.contains(compra) ? compra : entityManager.merge(compra));
    }

    private void salvarItens(Long compraId, List<CompraItemDTO> itens) {
        if (itens == null) {
            return;
        }

        for (CompraItemDTO itemDTO : itens) {
            CompraItem item = new CompraItem();
            item.setCompraId(compraId);
            item.setIngredienteId(itemDTO.getIngredienteId());
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorUnitario(itemDTO.getValorUnitario());
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
                item.getValorUnitario()
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