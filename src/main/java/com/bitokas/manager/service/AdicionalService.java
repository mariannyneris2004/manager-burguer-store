package com.bitokas.manager.service;

import com.bitokas.manager.dto.AdicionalDTO;
import com.bitokas.manager.dto.AdicionalIngredienteDTO;
import com.bitokas.manager.model.produtos.Adicional;
import com.bitokas.manager.model.produtos.AdicionalIngrediente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AdicionalService {

    @PersistenceContext
    private EntityManager entityManager;

    public AdicionalDTO cadastrar(AdicionalDTO dto) {
        Adicional adicional = new Adicional();
        adicional.setNome(dto.getNome());
        adicional.setValorBase(dto.getValorBase());

        entityManager.persist(adicional);
        entityManager.flush();

        salvarIngredientesDoAdicional(adicional.getId(), dto.getIngredientes());
        return buscarPorId(adicional.getId());
    }

    public AdicionalDTO atualizar(Long id, AdicionalDTO dto) {
        Adicional adicional = buscarEntidadePorId(id);
        adicional.setNome(dto.getNome());
        adicional.setValorBase(dto.getValorBase());
        entityManager.merge(adicional);

        entityManager.createQuery("delete from AdicionalIngrediente ai where ai.adicionalId = :adicionalId")
                .setParameter("adicionalId", id)
                .executeUpdate();

        salvarIngredientesDoAdicional(id, dto.getIngredientes());
        return buscarPorId(id);
    }

    public AdicionalDTO buscarPorId(Long id) {
        Adicional adicional = buscarEntidadePorId(id);
        return toDTOCompleto(adicional);
    }

    public List<AdicionalDTO> listarTodos() {
        return entityManager.createQuery("select a from Adicional a order by a.nome", Adicional.class)
                .getResultList()
                .stream()
                .map(this::toDTOCompleto)
                .toList();
    }

    public void excluir(Long id) {
        entityManager.createQuery("delete from AdicionalIngrediente ai where ai.adicionalId = :adicionalId")
                .setParameter("adicionalId", id)
                .executeUpdate();

        Adicional adicional = buscarEntidadePorId(id);
        entityManager.remove(entityManager.contains(adicional) ? adicional : entityManager.merge(adicional));
    }

    public List<AdicionalIngredienteDTO> listarIngredientesDoAdicional(Long adicionalId) {
        return entityManager.createQuery(
                        "select ai from AdicionalIngrediente ai where ai.adicionalId = :adicionalId",
                        AdicionalIngrediente.class)
                .setParameter("adicionalId", adicionalId)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private void salvarIngredientesDoAdicional(Long adicionalId, List<AdicionalIngredienteDTO> ingredientes) {
        if (ingredientes == null) {
            return;
        }
        for (AdicionalIngredienteDTO item : ingredientes) {
            AdicionalIngrediente ai = new AdicionalIngrediente();
            ai.setAdicionalId(adicionalId);
            ai.setIngredienteId(item.getIngredienteId());
            ai.setQuantidade(item.getQuantidade());
            entityManager.persist(ai);
        }
    }

    private Adicional buscarEntidadePorId(Long id) {
        Adicional adicional = entityManager.find(Adicional.class, id);
        if (adicional == null) {
            throw new IllegalArgumentException("Adicional não encontrado: " + id);
        }
        return adicional;
    }

    private AdicionalDTO toDTOCompleto(Adicional adicional) {
        return new AdicionalDTO(
                adicional.getId(),
                adicional.getNome(),
                adicional.getValorBase(),
                listarIngredientesDoAdicional(adicional.getId())
        );
    }

    private AdicionalIngredienteDTO toDTO(AdicionalIngrediente item) {
        return new AdicionalIngredienteDTO(
                item.getId(),
                item.getAdicionalId(),
                item.getIngredienteId(),
                item.getQuantidade()
        );
    }
}