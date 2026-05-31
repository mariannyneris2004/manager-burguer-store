package com.bitokas.manager.service;

import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.model.gastos.Estoque;
import com.bitokas.manager.model.produtos.Ingrediente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class IngredienteService {

    @PersistenceContext
    private EntityManager entityManager;

    public IngredienteDTO cadastrar(IngredienteDTO dto) {
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNome(dto.getNome());
        ingrediente.setMarca(dto.getMarca());
        ingrediente.setUnidadeConsumo(defaultString(dto.getUnidadeConsumo(), "UNIDADE"));
        ingrediente.setUnidadeCompra(defaultString(dto.getUnidadeCompra(), "PACOTE"));
        ingrediente.setQuantidadePorUnidadeCompra(defaultNumber(dto.getQuantidadePorUnidadeCompra(), 1d));

        entityManager.persist(ingrediente);
        entityManager.flush();

        return toDTO(ingrediente);
    }

    public IngredienteDTO atualizar(Long id, IngredienteDTO dto) {
        Ingrediente ingrediente = buscarEntidadePorId(id);
        ingrediente.setNome(dto.getNome());
        ingrediente.setMarca(dto.getMarca());
        ingrediente.setUnidadeConsumo(defaultString(dto.getUnidadeConsumo(), "UNIDADE"));
        ingrediente.setUnidadeCompra(defaultString(dto.getUnidadeCompra(), "PACOTE"));
        ingrediente.setQuantidadePorUnidadeCompra(defaultNumber(dto.getQuantidadePorUnidadeCompra(), 1d));

        entityManager.merge(ingrediente);
        return toDTO(ingrediente);
    }

    public IngredienteDTO buscarPorId(Long id) {
        return toDTO(buscarEntidadePorId(id));
    }

    public List<IngredienteDTO> listarTodos() {
        return entityManager
                .createQuery("select i from Ingrediente i order by i.nome", Ingrediente.class)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public void excluir(Long id) {
        Long estoqueCount = entityManager.createQuery(
                        "select count(e) from Estoque e where e.ingredienteId = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();

        Long prodCount = entityManager.createQuery(
                        "select count(pi) from ProdutoIngrediente pi where pi.ingredienteId = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();

        Long adicCount = entityManager.createQuery(
                        "select count(ai) from AdicionalIngrediente ai where ai.ingredienteId = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();

        if (estoqueCount > 0 || prodCount > 0 || adicCount > 0) {
            throw new IllegalStateException("Ingrediente não pode ser excluído porque está vinculado a estoque, produtos ou adicionais.");
        }

        Ingrediente ingrediente = buscarEntidadePorId(id);
        entityManager.remove(entityManager.contains(ingrediente) ? ingrediente : entityManager.merge(ingrediente));
    }

    public List<IngredienteDTO> buscarPorNome(String nome) {
        return entityManager.createQuery(
                        "select i from Ingrediente i where lower(i.nome) like lower(concat('%', :nome, '%')) order by i.nome",
                        Ingrediente.class)
                .setParameter("nome", nome)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private Ingrediente buscarEntidadePorId(Long id) {
        Ingrediente ingrediente = entityManager.find(Ingrediente.class, id);
        if (ingrediente == null) {
            throw new IllegalArgumentException("Ingrediente não encontrado: " + id);
        }
        return ingrediente;
    }

    private IngredienteDTO toDTO(Ingrediente ingrediente) {
        Estoque estoque = entityManager.createQuery(
                        "select e from Estoque e where e.ingredienteId = :id", Estoque.class)
                .setParameter("id", ingrediente.getId())
                .getResultStream()
                .findFirst()
                .orElse(null);

        double quantidade = estoque != null ? n(estoque.getQuantidade()) : 0d;
        double custoMedio = estoque != null ? n(estoque.getCustoMedioAtual()) : 0d;

        return new IngredienteDTO(
                ingrediente.getId(),
                ingrediente.getNome(),
                ingrediente.getMarca(),
                ingrediente.getUnidadeConsumo(),
                ingrediente.getUnidadeCompra(),
                ingrediente.getQuantidadePorUnidadeCompra(),
                quantidade,
                custoMedio,
                quantidade * custoMedio
        );
    }

    private double n(Double valor) {
        return valor == null ? 0d : valor;
    }

    private Double defaultNumber(Double valor, Double fallback) {
        return valor == null ? fallback : valor;
    }

    private String defaultString(String valor, String fallback) {
        return valor == null || valor.isBlank() ? fallback : valor;
    }
}
