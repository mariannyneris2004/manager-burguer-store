package com.bitokas.manager.service;

import com.bitokas.manager.dto.DespesaGeralDTO;
import com.bitokas.manager.model.gastos.DespesaGeral;
import com.bitokas.manager.model.gastos.Frequencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DespesaGeralService {

    @PersistenceContext
    private EntityManager entityManager;

    public DespesaGeralDTO cadastrar(DespesaGeralDTO dto) {
        DespesaGeral despesa = new DespesaGeral();
        despesa.setNome(dto.getNome());
        despesa.setValor(dto.getValor());
        despesa.setDataDespesa(dto.getDataDespesa());
        despesa.setFrequencia(dto.getFrequencia());

        entityManager.persist(despesa);
        entityManager.flush();

        return toDTO(despesa);
    }

    public DespesaGeralDTO atualizar(Long id, DespesaGeralDTO dto) {
        DespesaGeral despesa = buscarEntidadePorId(id);
        despesa.setNome(dto.getNome());
        despesa.setValor(dto.getValor());
        despesa.setDataDespesa(dto.getDataDespesa());
        despesa.setFrequencia(dto.getFrequencia());

        entityManager.merge(despesa);
        return toDTO(despesa);
    }

    public DespesaGeralDTO buscarPorId(Long id) {
        return toDTO(buscarEntidadePorId(id));
    }

    public List<DespesaGeralDTO> listarTodas() {
        return entityManager.createQuery("select d from DespesaGeral d order by d.dataDespesa desc", DespesaGeral.class)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<DespesaGeralDTO> listarPorFrequencia(Frequencia frequencia) {
        return entityManager.createQuery(
                        "select d from DespesaGeral d where d.frequencia = :frequencia order by d.dataDespesa desc",
                        DespesaGeral.class)
                .setParameter("frequencia", frequencia)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<DespesaGeralDTO> listarPorPeriodo(Date inicio, Date fim) {
        if (inicio == null && fim == null){
            return null;
        }
        return entityManager.createQuery(
                        "select d from DespesaGeral d where d.dataDespesa >= :inicio and d.dataDespesa < :fim order by d.dataDespesa desc",
                        DespesaGeral.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public void excluir(Long id) {
        DespesaGeral despesa = buscarEntidadePorId(id);
        entityManager.remove(entityManager.contains(despesa) ? despesa : entityManager.merge(despesa));
    }

    private DespesaGeral buscarEntidadePorId(Long id) {
        DespesaGeral despesa = entityManager.find(DespesaGeral.class, id);
        if (despesa == null) {
            throw new IllegalArgumentException("Despesa não encontrada: " + id);
        }
        return despesa;
    }

    private DespesaGeralDTO toDTO(DespesaGeral despesa) {
        return new DespesaGeralDTO(
                despesa.getId(),
                despesa.getNome(),
                despesa.getValor(),
                despesa.getDataDespesa(),
                despesa.getFrequencia()
        );
    }
}