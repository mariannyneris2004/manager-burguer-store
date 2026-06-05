package com.bitokas.manager.service;

import com.bitokas.manager.dto.EstoqueDTO;
import com.bitokas.manager.dto.IngredienteDTO;
import com.bitokas.manager.model.gastos.CompraItem;
import com.bitokas.manager.model.gastos.Estoque;
import com.bitokas.manager.model.gastos.MovimentoEstoque;
import com.bitokas.manager.model.gastos.TipoMovimentoEstoque;
import com.bitokas.manager.model.produtos.AdicionalIngrediente;
import com.bitokas.manager.model.produtos.ProdutoIngrediente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class EstoqueService {

    @PersistenceContext
    private EntityManager entityManager;

    private final IngredienteService ingredienteService;

    public EstoqueDTO buscarPorIngrediente(Long ingredienteId) {
        return toDTO(buscarOuCriar(ingredienteId));
    }

    public List<EstoqueDTO> listarTodos() {
        List<IngredienteDTO> ingredientes = ingredienteService.listarTodos();
        List<EstoqueDTO> estoqueDTOList = new ArrayList<>();

        for (IngredienteDTO ingrediente : ingredientes) {
            Estoque estoque = buscarEntidadePorIngrediente(ingrediente.getId());

            if (estoque == null) {
                estoqueDTOList.add(new EstoqueDTO(null, ingrediente.getId(), 0d, 0d, 0d));
            } else {
                estoqueDTOList.add(toDTO(estoque));
            }
        }

        return estoqueDTOList;
    }

    public List<MovimentoEstoque> listarMovimentosDoIngrediente(Long ingredienteId) {
        return entityManager.createQuery(
                        "select m from MovimentoEstoque m where m.ingredienteId = :ingredienteId order by m.dataHora desc, m.id desc",
                        MovimentoEstoque.class)
                .setParameter("ingredienteId", ingredienteId)
                .getResultList();
    }

    public EstoqueDTO ajustarEstoque(Long ingredienteId, Double quantidade) {
        double qtd = n(quantidade);

        if (qtd >= 0) {
            return registrarEntradaAjuste(ingredienteId, qtd, "Ajuste manual positivo");
        }

        return registrarSaidaAjuste(ingredienteId, Math.abs(qtd), "Ajuste manual negativo");
    }

    public double registrarEntradaCompra(Long ingredienteId,
                                         Double quantidadeEstoque,
                                         Double custoUnitarioEstoque,
                                         Long compraId) {
        Estoque estoque = buscarOuCriar(ingredienteId);

        double qtdAtual = n(estoque.getQuantidade());
        double custoAtual = n(estoque.getCustoMedioAtual());

        double qtdNova = qtdAtual + n(quantidadeEstoque);
        double valorAtual = qtdAtual * custoAtual;
        double valorNovo = n(quantidadeEstoque) * n(custoUnitarioEstoque);

        estoque.setQuantidade(qtdNova);
        estoque.setCustoMedioAtual(qtdNova == 0 ? 0d : (valorAtual + valorNovo) / qtdNova);
        entityManager.merge(estoque);

        registrarMovimento(
                ingredienteId,
                TipoMovimentoEstoque.ENTRADA_COMPRA,
                quantidadeEstoque,
                custoUnitarioEstoque,
                valorNovo,
                "COMPRA",
                compraId,
                null
        );

        return valorNovo;
    }

    public void devolverIngredienteDoPedido(Long ingredienteId,
                                           Double quantidade,
                                           Long pedidoId,
                                           String observacao) {
        registrarEntradaEstorno(ingredienteId, n(quantidade), pedidoId, observacao);
    }

    public double baixarIngredientesDoPedidoPersonalizado(Long pedidoId,
                                                          Map<Long, Double> consumos,
                                                          String observacao) {
        if (consumos == null || consumos.isEmpty()) {
            return 0d;
        }
        double custoTotal = 0d;
        for (Map.Entry<Long, Double> entry : consumos.entrySet()) {
            custoTotal += registrarSaidaPedido(entry.getKey(), n(entry.getValue()), pedidoId, observacao);
        }
        return custoTotal;
    }

    public void devolverMovimentosDoPedido(Long pedidoId) {
        List<MovimentoEstoque> movimentos = entityManager.createQuery(
                        "select m from MovimentoEstoque m where m.origemTipo = 'PEDIDO' and m.origemId = :pedidoId and m.tipo = :tipo",
                        MovimentoEstoque.class)
                .setParameter("pedidoId", pedidoId)
                .setParameter("tipo", TipoMovimentoEstoque.SAIDA_PEDIDO)
                .getResultList();

        for (MovimentoEstoque movimento : movimentos) {
            registrarEntradaEstorno(
                    movimento.getIngredienteId(),
                    movimento.getQuantidade(),
                    pedidoId,
                    "Estorno do pedido " + pedidoId
            );
        }
    }

    public double baixarIngredientesDoProduto(Long produtoId,
                                              Integer quantidadeProduto,
                                              Long pedidoId) {
        return baixarIngredientesDoProdutoPersonalizado(produtoId, quantidadeProduto, List.of(), pedidoId);
    }

    public double baixarIngredientesDoProdutoPersonalizado(Long produtoId,
                                                          Integer quantidadeProduto,
                                                          Collection<Long> ingredientesRetirados,
                                                          Long pedidoId) {
        List<ProdutoIngrediente> itens = entityManager.createQuery(
                        "select pi from ProdutoIngrediente pi where pi.produtoId = :produtoId",
                        ProdutoIngrediente.class)
                .setParameter("produtoId", produtoId)
                .getResultList();

        int multiplicador = quantidadeProduto == null ? 1 : quantidadeProduto;
        double custoTotal = 0d;
        Collection<Long> retirados = ingredientesRetirados == null ? List.of() : ingredientesRetirados;

        for (ProdutoIngrediente item : itens) {
            if (retirados.contains(item.getIngredienteId())) {
                continue;
            }
            custoTotal += registrarSaidaPedido(
                    item.getIngredienteId(),
                    n(item.getQuantidade()) * multiplicador,
                    pedidoId,
                    "Baixa de produto " + produtoId
            );
        }

        return custoTotal;
    }

    public double baixarIngredientesDoAdicional(Long adicionalId,
                                                Integer quantidadeAdicional,
                                                Long pedidoId) {
        List<AdicionalIngrediente> itens = entityManager.createQuery(
                        "select ai from AdicionalIngrediente ai where ai.adicionalId = :adicionalId",
                        AdicionalIngrediente.class)
                .setParameter("adicionalId", adicionalId)
                .getResultList();

        int multiplicador = quantidadeAdicional == null ? 1 : quantidadeAdicional;
        double custoTotal = 0d;

        for (AdicionalIngrediente item : itens) {
            custoTotal += registrarSaidaPedido(
                    item.getIngredienteId(),
                    n(item.getQuantidade()) * multiplicador,
                    pedidoId,
                    "Baixa de adicional " + adicionalId
            );
        }

        return custoTotal;
    }

    public void devolverIngredientesDoProduto(Long produtoId,
                                              Integer quantidadeProduto,
                                              Long pedidoId) {
        List<ProdutoIngrediente> itens = entityManager.createQuery(
                        "select pi from ProdutoIngrediente pi where pi.produtoId = :produtoId",
                        ProdutoIngrediente.class)
                .setParameter("produtoId", produtoId)
                .getResultList();

        int multiplicador = quantidadeProduto == null ? 1 : quantidadeProduto;

        for (ProdutoIngrediente item : itens) {
            registrarEntradaEstorno(
                    item.getIngredienteId(),
                    n(item.getQuantidade()) * multiplicador,
                    pedidoId,
                    "Estorno de produto " + produtoId
            );
        }
    }

    public void devolverIngredientesDoAdicional(Long adicionalId,
                                                Integer quantidadeAdicional,
                                                Long pedidoId) {
        List<AdicionalIngrediente> itens = entityManager.createQuery(
                        "select ai from AdicionalIngrediente ai where ai.adicionalId = :adicionalId",
                        AdicionalIngrediente.class)
                .setParameter("adicionalId", adicionalId)
                .getResultList();

        int multiplicador = quantidadeAdicional == null ? 1 : quantidadeAdicional;

        for (AdicionalIngrediente item : itens) {
            registrarEntradaEstorno(
                    item.getIngredienteId(),
                    n(item.getQuantidade()) * multiplicador,
                    pedidoId,
                    "Estorno de adicional " + adicionalId
            );
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

    private Estoque buscarOuCriar(Long ingredienteId) {
        Estoque estoque = buscarEntidadePorIngrediente(ingredienteId);
        if (estoque == null) {
            estoque = new Estoque();
            estoque.setIngredienteId(ingredienteId);
            estoque.setQuantidade(0d);
            estoque.setCustoMedioAtual(0d);
            entityManager.persist(estoque);
            entityManager.flush();
        }
        return estoque;
    }

    private EstoqueDTO registrarEntradaAjuste(Long ingredienteId, Double quantidade, String observacao) {
        Estoque estoque = buscarOuCriar(ingredienteId);

        double custoUnitario = n(estoque.getCustoMedioAtual());
        double valorTotal = quantidade * custoUnitario;

        estoque.setQuantidade(n(estoque.getQuantidade()) + quantidade);
        entityManager.merge(estoque);

        registrarMovimento(
                ingredienteId,
                TipoMovimentoEstoque.AJUSTE_POSITIVO,
                quantidade,
                custoUnitario,
                valorTotal,
                "AJUSTE",
                null,
                observacao
        );

        return toDTO(estoque);
    }

    private EstoqueDTO registrarSaidaAjuste(Long ingredienteId, Double quantidade, String observacao) {
        Estoque estoque = buscarOuCriar(ingredienteId);

        if (n(estoque.getQuantidade()) < quantidade) {
            throw new IllegalStateException("Estoque insuficiente para ajuste do ingrediente " + ingredienteId);
        }

        double custoUnitario = n(estoque.getCustoMedioAtual());
        double valorTotal = quantidade * custoUnitario;

        estoque.setQuantidade(n(estoque.getQuantidade()) - quantidade);
        entityManager.merge(estoque);

        registrarMovimento(
                ingredienteId,
                TipoMovimentoEstoque.AJUSTE_NEGATIVO,
                quantidade,
                custoUnitario,
                valorTotal,
                "AJUSTE",
                null,
                observacao
        );

        return toDTO(estoque);
    }

    private double registrarSaidaPedido(Long ingredienteId,
                                        Double quantidade,
                                        Long pedidoId,
                                        String observacao) {
        Estoque estoque = buscarOuCriar(ingredienteId);

        if (n(estoque.getQuantidade()) < quantidade) {
            throw new IllegalStateException("Estoque insuficiente para o ingrediente " + ingredienteId);
        }

        double custoUnitario = n(estoque.getCustoMedioAtual());
        double valorTotal = quantidade * custoUnitario;

        estoque.setQuantidade(n(estoque.getQuantidade()) - quantidade);
        entityManager.merge(estoque);

        registrarMovimento(
                ingredienteId,
                TipoMovimentoEstoque.SAIDA_PEDIDO,
                quantidade,
                custoUnitario,
                valorTotal,
                "PEDIDO",
                pedidoId,
                observacao
        );

        return valorTotal;
    }

    private void registrarEntradaEstorno(Long ingredienteId,
                                         Double quantidade,
                                         Long pedidoId,
                                         String observacao) {
        Estoque estoque = buscarOuCriar(ingredienteId);

        double custoUnitario = n(estoque.getCustoMedioAtual());
        double valorTotal = quantidade * custoUnitario;

        double qtdAtual = n(estoque.getQuantidade());
        double qtdNova = qtdAtual + quantidade;

        estoque.setQuantidade(qtdNova);
        estoque.setCustoMedioAtual(qtdNova == 0 ? 0d : custoUnitario);
        entityManager.merge(estoque);

        registrarMovimento(
                ingredienteId,
                TipoMovimentoEstoque.ESTORNO_PEDIDO,
                quantidade,
                custoUnitario,
                valorTotal,
                "PEDIDO",
                pedidoId,
                observacao
        );
    }

    private void registrarMovimento(Long ingredienteId,
                                    TipoMovimentoEstoque tipo,
                                    Double quantidade,
                                    Double custoUnitario,
                                    Double valorTotal,
                                    String origemTipo,
                                    Long origemId,
                                    String observacao) {
        MovimentoEstoque mov = new MovimentoEstoque();
        mov.setIngredienteId(ingredienteId);
        mov.setDataHora(LocalDateTime.now());
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setCustoUnitario(custoUnitario);
        mov.setValorTotal(valorTotal);
        mov.setOrigemTipo(origemTipo);
        mov.setOrigemId(origemId == null ? 0L : origemId);
        mov.setObservacao(observacao);
        entityManager.persist(mov);
    }

    private EstoqueDTO toDTO(Estoque estoque) {
        double quantidade = n(estoque.getQuantidade());
        double custoMedio = n(estoque.getCustoMedioAtual());
        return new EstoqueDTO(
                estoque.getId(),
                estoque.getIngredienteId(),
                quantidade,
                custoMedio,
                quantidade * custoMedio
        );
    }

    private double n(Double valor) {
        return valor == null ? 0d : valor;
    }
}
