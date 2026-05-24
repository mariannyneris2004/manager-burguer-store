package com.bitokas.manager.service;

import com.bitokas.manager.dto.IngredienteDTO;
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
        ingrediente.setMedidaUnitaria(dto.getMedidaUnitaria());
        ingrediente.setValor(dto.getValor());

        entityManager.persist(ingrediente);
        entityManager.flush();

        return toDTO(ingrediente);
    }

    public IngredienteDTO atualizar(Long id, IngredienteDTO dto) {
        Ingrediente ingrediente = buscarEntidadePorId(id);
        ingrediente.setNome(dto.getNome());
        ingrediente.setMarca(dto.getMarca());
        ingrediente.setMedidaUnitaria(dto.getMedidaUnitaria());
        ingrediente.setValor(dto.getValor());

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
        return new IngredienteDTO(
                ingrediente.getId(),
                ingrediente.getNome(),
                ingrediente.getMarca(),
                ingrediente.getMedidaUnitaria(),
                ingrediente.getValor()
        );
    }
}