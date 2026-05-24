package com.bitokas.manager.service;

import com.bitokas.manager.dto.ProdutoAdicionalDTO;
import com.bitokas.manager.dto.ProdutoDTO;
import com.bitokas.manager.dto.ProdutoIngredienteDTO;
import com.bitokas.manager.model.produtos.Produto;
import com.bitokas.manager.model.produtos.ProdutoAdicional;
import com.bitokas.manager.model.produtos.ProdutoIngrediente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProdutoService {

    @PersistenceContext
    private EntityManager entityManager;

    public ProdutoDTO cadastrar(ProdutoDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setValorVenda(dto.getValorVenda());
        produto.setCategoria(dto.getCategoria());

        entityManager.persist(produto);
        entityManager.flush();

        salvarIngredientesDoProduto(produto.getId(), dto.getIngredientes());
        salvarAdicionaisPermitidos(produto.getId(), dto.getAdicionaisPermitidos());

        return buscarPorId(produto.getId());
    }

    public ProdutoDTO atualizar(Long id, ProdutoDTO dto) {
        Produto produto = buscarEntidadePorId(id);
        produto.setNome(dto.getNome());
        produto.setValorVenda(dto.getValorVenda());
        produto.setCategoria(dto.getCategoria());
        entityManager.merge(produto);

        entityManager.createQuery("delete from ProdutoIngrediente pi where pi.produtoId = :produtoId")
                .setParameter("produtoId", id)
                .executeUpdate();

        entityManager.createQuery("delete from ProdutoAdicional pa where pa.produtoId = :produtoId")
                .setParameter("produtoId", id)
                .executeUpdate();

        salvarIngredientesDoProduto(id, dto.getIngredientes());
        salvarAdicionaisPermitidos(id, dto.getAdicionaisPermitidos());

        return buscarPorId(id);
    }

    public ProdutoDTO buscarPorId(Long id) {
        Produto produto = buscarEntidadePorId(id);
        return toDTOCompleto(produto);
    }

    public List<ProdutoDTO> listarTodos() {
        return entityManager.createQuery("select p from Produto p order by p.nome", Produto.class)
                .getResultList()
                .stream()
                .map(this::toDTOCompleto)
                .toList();
    }

    public List<ProdutoDTO> listarPorCategoria(String categoria) {
        return entityManager.createQuery(
                        "select p from Produto p where lower(p.categoria) = lower(:categoria) order by p.nome",
                        Produto.class)
                .setParameter("categoria", categoria)
                .getResultList()
                .stream()
                .map(this::toDTOCompleto)
                .toList();
    }

    public void excluir(Long id) {
        entityManager.createQuery("delete from ProdutoIngrediente pi where pi.produtoId = :produtoId")
                .setParameter("produtoId", id)
                .executeUpdate();

        entityManager.createQuery("delete from ProdutoAdicional pa where pa.produtoId = :produtoId")
                .setParameter("produtoId", id)
                .executeUpdate();

        Produto produto = buscarEntidadePorId(id);
        entityManager.remove(entityManager.contains(produto) ? produto : entityManager.merge(produto));
    }

    public List<ProdutoIngredienteDTO> listarIngredientesDoProduto(Long produtoId) {
        return entityManager.createQuery(
                        "select pi from ProdutoIngrediente pi where pi.produtoId = :produtoId",
                        ProdutoIngrediente.class)
                .setParameter("produtoId", produtoId)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ProdutoAdicionalDTO> listarAdicionaisPermitidos(Long produtoId) {
        return entityManager.createQuery(
                        "select pa from ProdutoAdicional pa where pa.produtoId = :produtoId",
                        ProdutoAdicional.class)
                .setParameter("produtoId", produtoId)
                .getResultList()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private void salvarIngredientesDoProduto(Long produtoId, List<ProdutoIngredienteDTO> ingredientes) {
        if (ingredientes == null) {
            return;
        }
        for (ProdutoIngredienteDTO item : ingredientes) {
            ProdutoIngrediente pi = new ProdutoIngrediente();
            pi.setProdutoId(produtoId);
            pi.setIngredienteId(item.getIngredienteId());
            pi.setQuantidade(item.getQuantidade());
            entityManager.persist(pi);
        }
    }

    private void salvarAdicionaisPermitidos(Long produtoId, List<ProdutoAdicionalDTO> adicionais) {
        if (adicionais == null) {
            return;
        }
        for (ProdutoAdicionalDTO item : adicionais) {
            ProdutoAdicional pa = new ProdutoAdicional();
            pa.setProdutoId(produtoId);
            pa.setAdicionalId(item.getAdicionalId());
            entityManager.persist(pa);
        }
    }

    private Produto buscarEntidadePorId(Long id) {
        Produto produto = entityManager.find(Produto.class, id);
        if (produto == null) {
            throw new IllegalArgumentException("Produto não encontrado: " + id);
        }
        return produto;
    }

    private ProdutoDTO toDTOCompleto(Produto produto) {
        return new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getValorVenda(),
                produto.getCategoria(),
                listarIngredientesDoProduto(produto.getId()),
                listarAdicionaisPermitidos(produto.getId())
        );
    }

    private ProdutoIngredienteDTO toDTO(ProdutoIngrediente item) {
        return new ProdutoIngredienteDTO(
                item.getId(),
                item.getProdutoId(),
                item.getIngredienteId(),
                item.getQuantidade()
        );
    }

    private ProdutoAdicionalDTO toDTO(ProdutoAdicional item) {
        return new ProdutoAdicionalDTO(
                item.getId(),
                item.getProdutoId(),
                Long.valueOf(item.getAdicionalId())
        );
    }
}