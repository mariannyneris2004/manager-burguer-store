package com.bitokas.manager.service;

import com.bitokas.manager.dto.PedidoDTO;
import com.bitokas.manager.dto.PedidoItemAdicionalDTO;
import com.bitokas.manager.dto.PedidoItemDTO;
import com.bitokas.manager.dto.PedidoItemRetiradaDTO;
import com.bitokas.manager.model.gastos.MovimentoEstoque;
import com.bitokas.manager.model.gastos.TipoMovimentoEstoque;
import com.bitokas.manager.model.pedidos.*;
import com.bitokas.manager.model.produtos.Adicional;
import com.bitokas.manager.model.produtos.AdicionalIngrediente;
import com.bitokas.manager.model.produtos.Ingrediente;
import com.bitokas.manager.model.produtos.Produto;
import com.bitokas.manager.model.produtos.ProdutoIngrediente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class PedidoService {

    @PersistenceContext
    private EntityManager entityManager;

    private final EstoqueService estoqueService;

    public PedidoDTO registrarPedido(PedidoDTO dto) {
        validarPedido(dto);

        List<ItemMontado> itensMontados = montarItens(dto.getItens());
        Pedido pedido = montarPedidoBase(dto);
        entityManager.persist(pedido);
        entityManager.flush();

        persistirItens(pedido.getId(), itensMontados);
        estoqueService.baixarIngredientesDoPedidoPersonalizado(
                pedido.getId(),
                juntarConsumos(itensMontados),
                "Baixa do pedido " + pedido.getId()
        );

        double valorItens = itensMontados.stream().mapToDouble(i -> i.item().getValorTotalItem()).sum();
        double custoItens = itensMontados.stream().mapToDouble(i -> i.item().getCustoTotalItem()).sum();
        double valorTotal = valorItens + valorEntrega(dto);

        pedido.setValorTotal(valorTotal);
        pedido.setCustoTotal(custoItens);
        pedido.setValorPago(dto.getValorPago() == null ? valorTotal : dto.getValorPago());
        pedido.setLucroBruto(pedido.getValorPago() - custoItens);
        entityManager.merge(pedido);

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
        if (dto == null || dto.getItens() == null || dto.getItens().isEmpty()) {
            return 0d;
        }
        double valorItens = montarItens(dto.getItens()).stream()
                .mapToDouble(item -> item.item().getValorTotalItem())
                .sum();
        return valorItens + valorEntrega(dto);
    }

    public PedidoDTO atualizar(Long id, PedidoDTO dto) {
        validarPedido(dto);

        Pedido pedido = buscarEntidadePorId(id);
        devolverConsumosDoPedido(id);
        limparItensDoPedido(id);

        List<ItemMontado> itensMontados = montarItens(dto.getItens());
        persistirItens(id, itensMontados);
        estoqueService.baixarIngredientesDoPedidoPersonalizado(
                id,
                juntarConsumos(itensMontados),
                "Baixa do pedido " + id
        );

        double valorItens = itensMontados.stream().mapToDouble(i -> i.item().getValorTotalItem()).sum();
        double custoItens = itensMontados.stream().mapToDouble(i -> i.item().getCustoTotalItem()).sum();
        double valorTotal = valorItens + valorEntrega(dto);

        pedido.setNomeCliente(dto.getNomeCliente());
        pedido.setTipoEntrega(dto.getTipoEntrega());
        pedido.setValorEntrega(dto.getValorEntrega());
        pedido.setDataHora(dto.getDataHora() == null ? pedido.getDataHora() : dto.getDataHora());
        pedido.setValorTotal(valorTotal);
        pedido.setCustoTotal(custoItens);
        pedido.setValorPago(dto.getValorPago() == null ? valorTotal : dto.getValorPago());
        pedido.setLucroBruto(dto.getValorPago() - custoItens);
        entityManager.merge(pedido);

        return buscarPorId(id);
    }

    public void cancelarPedido(Long id) {
        buscarEntidadePorId(id);
        devolverConsumosDoPedido(id);
        limparItensDoPedido(id);
        Pedido pedido = buscarEntidadePorId(id);
        entityManager.remove(entityManager.contains(pedido) ? pedido : entityManager.merge(pedido));
    }

    public void excluir(Long id) {
        cancelarPedido(id);
    }

    private Pedido montarPedidoBase(PedidoDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setNomeCliente(dto.getNomeCliente());
        pedido.setDataHora(dto.getDataHora() == null ? LocalDateTime.now() : dto.getDataHora());
        pedido.setTipoEntrega(dto.getTipoEntrega());
        pedido.setValorEntrega(dto.getValorEntrega());
        pedido.setValorTotal(0d);
        pedido.setValorPago(dto.getValorPago() == null ? 0d : dto.getValorPago());
        pedido.setCustoTotal(0d);
        pedido.setLucroBruto(0d);
        return pedido;
    }

    private List<ItemMontado> montarItens(List<PedidoItemDTO> itensEntrada) {
        List<ItemMontado> itensMontados = new ArrayList<>();
        if (itensEntrada == null) {
            return itensMontados;
        }

        for (PedidoItemDTO itemEntrada : itensEntrada) {
            if (itemEntrada == null || itemEntrada.getProdutoId() == null || n(itemEntrada.getQuantidade()) <= 0) {
                continue;
            }
            itensMontados.add(montarItem(itemEntrada));
        }

        if (itensMontados.isEmpty()) {
            throw new IllegalArgumentException("O pedido precisa ter ao menos um item válido.");
        }

        return itensMontados;
    }

    private ItemMontado montarItem(PedidoItemDTO itemEntrada) {
        Produto produto = entityManager.find(Produto.class, itemEntrada.getProdutoId());
        if (produto == null) {
            throw new IllegalArgumentException("Produto não encontrado: " + itemEntrada.getProdutoId());
        }

        int quantidadeItem = itemEntrada.getQuantidade() == null || itemEntrada.getQuantidade() <= 0 ? 1 : itemEntrada.getQuantidade();

        Map<Long, Double> retiradasSelecionadas = new HashMap<>();
        if (itemEntrada.getRetiradas() != null) {
            for (PedidoItemRetiradaDTO retirada : itemEntrada.getRetiradas()) {
                if (retirada != null && retirada.getIngredienteId() != null) {
                    retiradasSelecionadas.put(retirada.getIngredienteId(), 1d);
                }
            }
        }

        Map<Long, PedidoItemAdicionalDTO> adicionaisSelecionados = new LinkedHashMap<>();
        if (itemEntrada.getAdicionais() != null) {
            for (PedidoItemAdicionalDTO adicional : itemEntrada.getAdicionais()) {
                if (adicional == null || adicional.getAdicionalId() == null) {
                    continue;
                }
                int quantidadeAdicional = adicional.getQuantidade() == null || adicional.getQuantidade() <= 0 ? 0 : adicional.getQuantidade();
                if (quantidadeAdicional <= 0) {
                    continue;
                }
                PedidoItemAdicionalDTO existente = adicionaisSelecionados.get(adicional.getAdicionalId());
                if (existente == null) {
                    adicionaisSelecionados.put(adicional.getAdicionalId(), new PedidoItemAdicionalDTO(
                            null,
                            null,
                            adicional.getAdicionalId(),
                            null,
                            null,
                            quantidadeAdicional,
                            null,
                            null
                    ));
                } else {
                    existente.setQuantidade(existente.getQuantidade() + quantidadeAdicional);
                }
            }
        }

        Set<Long> adicionaisPermitidos = new HashSet<>(listarAdicionaisPermitidosIds(produto.getId()));
        Set<Long> ingredientesProduto = new HashSet<>();
        List<ProdutoIngrediente> ingredientesDoProduto = listarIngredientesProduto(produto.getId());
        for (ProdutoIngrediente item : ingredientesDoProduto) {
            ingredientesProduto.add(item.getIngredienteId());
        }

        for (Long adicionalId : adicionaisSelecionados.keySet()) {
            if (!adicionaisPermitidos.contains(adicionalId)) {
                throw new IllegalArgumentException("Adicional não permitido para o produto " + produto.getNome() + ": " + adicionalId);
            }
        }

        double valorVendaUnitario = n(produto.getValorVenda());
        double custoBaseUnitario = 0d;
        double custoRetiradasUnitario = 0d;
        double valorAdicionaisUnitario = 0d;
        double custoAdicionaisUnitario = 0d;
        Map<Long, Double> consumosMapa = new HashMap<>();
        List<ConsumoMontado> consumos = new ArrayList<>();

        List<PedidoItemRetiradaDTO> retiradasSalvas = new ArrayList<>();
        for (ProdutoIngrediente ingrediente : ingredientesDoProduto) {
            double quantidadeReceita = n(ingrediente.getQuantidade());
            double custoUnitarioIngrediente = custoUnitarioIngrediente(ingrediente.getIngredienteId());
            boolean retirado = retiradasSelecionadas.containsKey(ingrediente.getIngredienteId());

            if (retirado) {
                double qtdTotalRetirada = quantidadeReceita * quantidadeItem;
                double custoTotalRetirada = qtdTotalRetirada * custoUnitarioIngrediente;
                custoRetiradasUnitario += quantidadeReceita * custoUnitarioIngrediente;
                retiradasSalvas.add(new PedidoItemRetiradaDTO(
                        null,
                        null,
                        ingrediente.getIngredienteId(),
                        nomeIngrediente(ingrediente.getIngredienteId()),
                        qtdTotalRetirada,
                        custoUnitarioIngrediente,
                        custoTotalRetirada
                ));
                continue;
            }

            double quantidadeConsumida = quantidadeReceita * quantidadeItem;
            custoBaseUnitario += quantidadeReceita * custoUnitarioIngrediente;
            consumosMapa.merge(ingrediente.getIngredienteId(), quantidadeConsumida, Double::sum);
            consumos.add(new ConsumoMontado(
                    ingrediente.getIngredienteId(),
                    nomeIngrediente(ingrediente.getIngredienteId()),
                    quantidadeConsumida,
                    custoUnitarioIngrediente,
                    quantidadeConsumida * custoUnitarioIngrediente,
                    "PRODUTO"
            ));
        }

        List<PedidoItemAdicionalDTO> adicionaisSalvos = new ArrayList<>();
        for (PedidoItemAdicionalDTO adicionalSel : adicionaisSelecionados.values()) {
            Adicional adicional = entityManager.find(Adicional.class, adicionalSel.getAdicionalId());
            if (adicional == null) {
                throw new IllegalArgumentException("Adicional não encontrado: " + adicionalSel.getAdicionalId());
            }

            List<AdicionalIngrediente> ingredientesAdicional = listarIngredientesAdicional(adicional.getId());
            double custoUnitarioAdicional = 0d;
            for (AdicionalIngrediente ingrediente : ingredientesAdicional) {
                double quantidadeReceita = n(ingrediente.getQuantidade());
                double custoUnitarioIngrediente = custoUnitarioIngrediente(ingrediente.getIngredienteId());
                custoUnitarioAdicional += quantidadeReceita * custoUnitarioIngrediente;
                double quantidadeConsumida = quantidadeReceita * quantidadeItem * adicionalSel.getQuantidade();
                consumosMapa.merge(ingrediente.getIngredienteId(), quantidadeConsumida, Double::sum);
                consumos.add(new ConsumoMontado(
                        ingrediente.getIngredienteId(),
                        nomeIngrediente(ingrediente.getIngredienteId()),
                        quantidadeConsumida,
                        custoUnitarioIngrediente,
                        quantidadeConsumida * custoUnitarioIngrediente,
                        "ADICIONAL"
                ));
            }

            double valorUnitarioAdicional = n(adicional.getValorBase()) * adicionalSel.getQuantidade();
            valorAdicionaisUnitario += valorUnitarioAdicional;
            custoAdicionaisUnitario += custoUnitarioAdicional * adicionalSel.getQuantidade();

            adicionaisSalvos.add(new PedidoItemAdicionalDTO(
                    null,
                    null,
                    adicional.getId(),
                    adicional.getNome(),
                    adicional.getValorBase(),
                    adicionalSel.getQuantidade(),
                    custoUnitarioAdicional,
                    custoUnitarioAdicional * adicionalSel.getQuantidade() * quantidadeItem
            ));
        }

        double valorTotalItem = (valorVendaUnitario + valorAdicionaisUnitario) * quantidadeItem;
        double custoTotalItem = (custoBaseUnitario + custoAdicionaisUnitario) * quantidadeItem;
        double custoBaseTotal = custoBaseUnitario * quantidadeItem;
        double custoRetiradasTotal = custoRetiradasUnitario * quantidadeItem;
        double custoAdicionaisTotal = custoAdicionaisUnitario * quantidadeItem;
        double lucroItem = valorTotalItem - custoTotalItem;

        PedidoItemDTO item = new PedidoItemDTO(
                null,
                null,
                produto.getId(),
                produto.getNome(),
                valorVendaUnitario,
                quantidadeItem,
                custoBaseUnitario,
                custoBaseTotal,
                valorAdicionaisUnitario * quantidadeItem,
                custoAdicionaisTotal,
                custoRetiradasTotal,
                valorTotalItem,
                custoTotalItem,
                lucroItem,
                adicionaisSalvos,
                retiradasSalvas
        );

        return new ItemMontado(item, consumosMapa, consumos);
    }

    private void persistirItens(Long pedidoId, List<ItemMontado> itensMontados) {
        for (ItemMontado montado : itensMontados) {
            PedidoItemDTO dto = montado.item();
            PedidoItem item = new PedidoItem();
            item.setPedidoId(pedidoId);
            item.setProdutoId(dto.getProdutoId());
            item.setProdutoNome(dto.getProdutoNome());
            item.setProdutoValorVenda(dto.getProdutoValorVenda());
            item.setQuantidade(dto.getQuantidade());
            item.setCustoBaseUnitario(dto.getCustoBaseUnitario());
            item.setCustoBaseTotal(dto.getCustoBaseTotal());
            item.setValorAdicionaisTotal(dto.getValorAdicionaisTotal());
            item.setCustoAdicionaisTotal(dto.getCustoAdicionaisTotal());
            item.setCustoRetiradasTotal(dto.getCustoRetiradasTotal());
            item.setValorTotalItem(dto.getValorTotalItem());
            item.setCustoTotalItem(dto.getCustoTotalItem());
            item.setLucroItem(dto.getLucroItem());
            entityManager.persist(item);
            entityManager.flush();

            for (PedidoItemAdicionalDTO adicionalDTO : dto.getAdicionais()) {
                PedidoItemAdicional adicional = new PedidoItemAdicional();
                adicional.setPedidoItemId(item.getId());
                adicional.setAdicionalId(adicionalDTO.getAdicionalId());
                adicional.setAdicionalNome(adicionalDTO.getAdicionalNome());
                adicional.setAdicionalValorBase(adicionalDTO.getAdicionalValorBase());
                adicional.setQuantidade(adicionalDTO.getQuantidade());
                adicional.setCustoUnitario(adicionalDTO.getCustoUnitario());
                adicional.setCustoTotal(adicionalDTO.getCustoTotal());
                entityManager.persist(adicional);
            }

            for (PedidoItemRetiradaDTO retiradaDTO : dto.getRetiradas()) {
                PedidoItemRetirada retirada = new PedidoItemRetirada();
                retirada.setPedidoItemId(item.getId());
                retirada.setIngredienteId(retiradaDTO.getIngredienteId());
                retirada.setIngredienteNome(retiradaDTO.getIngredienteNome());
                retirada.setQuantidade(retiradaDTO.getQuantidade());
                retirada.setCustoUnitario(retiradaDTO.getCustoUnitario());
                retirada.setCustoTotal(retiradaDTO.getCustoTotal());
                entityManager.persist(retirada);
            }

            for (ConsumoMontado consumo : montado.consumos()) {
                PedidoItemConsumo itemConsumo = new PedidoItemConsumo();
                itemConsumo.setPedidoItemId(item.getId());
                itemConsumo.setIngredienteId(consumo.ingredienteId());
                itemConsumo.setIngredienteNome(consumo.ingredienteNome());
                itemConsumo.setOrigemTipo(consumo.origemTipo());
                itemConsumo.setQuantidade(consumo.quantidade());
                itemConsumo.setCustoUnitario(consumo.custoUnitario());
                itemConsumo.setCustoTotal(consumo.custoTotal());
                entityManager.persist(itemConsumo);
            }
        }
    }

    private void limparItensDoPedido(Long pedidoId) {
        entityManager.createQuery("delete from PedidoItem pi where pi.pedidoId = :pedidoId")
                .setParameter("pedidoId", pedidoId)
                .executeUpdate();
        entityManager.createQuery("delete from PedidoAdicional pa where pa.pedidoId = :pedidoId")
                .setParameter("pedidoId", pedidoId)
                .executeUpdate();
        entityManager.createQuery("delete from PedidoProduto pp where pp.pedidoId = :pedidoId")
                .setParameter("pedidoId", pedidoId)
                .executeUpdate();
    }

    private void devolverConsumosDoPedido(Long pedidoId) {
        List<PedidoItemConsumo> consumos = entityManager.createQuery(
                        "select c from PedidoItemConsumo c where c.pedidoItemId in (select pi.id from PedidoItem pi where pi.pedidoId = :pedidoId)",
                        PedidoItemConsumo.class)
                .setParameter("pedidoId", pedidoId)
                .getResultList();

        for (PedidoItemConsumo consumo : consumos) {
            estoqueService.devolverIngredienteDoPedido(
                    consumo.getIngredienteId(),
                    consumo.getQuantidade(),
                    pedidoId,
                    "Estorno do pedido " + pedidoId
            );
        }
    }

    private Pedido buscarEntidadePorId(Long id) {
        Pedido pedido = entityManager.find(Pedido.class, id);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não encontrado: " + id);
        }
        return pedido;
    }

    private PedidoDTO toDTOCompleto(Pedido pedido) {
        List<PedidoItemDTO> itens = listarItensDoPedido(pedido.getId());
        return new PedidoDTO(
                pedido.getId(),
                pedido.getNomeCliente(),
                pedido.getDataHora(),
                pedido.getTipoEntrega(),
                pedido.getValorEntrega(),
                pedido.getValorTotal(),
                pedido.getValorPago(),
                pedido.getCustoTotal(),
                pedido.getLucroBruto(),
                itens
        );
    }

    private List<PedidoItemDTO> listarItensDoPedido(Long pedidoId) {
        List<PedidoItem> itensPersistidos = entityManager.createQuery(
                        "select pi from PedidoItem pi where pi.pedidoId = :pedidoId order by pi.id",
                        PedidoItem.class)
                .setParameter("pedidoId", pedidoId)
                .getResultList();

        if (!itensPersistidos.isEmpty()) {
            List<PedidoItemDTO> itens = new ArrayList<>();
            for (PedidoItem item : itensPersistidos) {
                itens.add(new PedidoItemDTO(
                        item.getId(),
                        item.getPedidoId(),
                        item.getProdutoId(),
                        item.getProdutoNome(),
                        item.getProdutoValorVenda(),
                        item.getQuantidade(),
                        item.getCustoBaseUnitario(),
                        item.getCustoBaseTotal(),
                        item.getValorAdicionaisTotal(),
                        item.getCustoAdicionaisTotal(),
                        item.getCustoRetiradasTotal(),
                        item.getValorTotalItem(),
                        item.getCustoTotalItem(),
                        item.getLucroItem(),
                        listarAdicionaisDoItem(item.getId()),
                        listarRetiradasDoItem(item.getId())
                ));
            }
            return itens;
        }

        return new ArrayList<>();
    }

    private List<PedidoItemAdicionalDTO> listarAdicionaisDoItem(Long pedidoItemId) {
        return entityManager.createQuery(
                        "select pa from PedidoItemAdicional pa where pa.pedidoItemId = :pedidoItemId order by pa.id",
                        PedidoItemAdicional.class)
                .setParameter("pedidoItemId", pedidoItemId)
                .getResultList()
                .stream()
                .map(item -> new PedidoItemAdicionalDTO(
                        item.getId(),
                        item.getPedidoItemId(),
                        item.getAdicionalId(),
                        item.getAdicionalNome(),
                        item.getAdicionalValorBase(),
                        item.getQuantidade(),
                        item.getCustoUnitario(),
                        item.getCustoTotal()
                ))
                .toList();
    }

    private List<PedidoItemRetiradaDTO> listarRetiradasDoItem(Long pedidoItemId) {
        return entityManager.createQuery(
                        "select pr from PedidoItemRetirada pr where pr.pedidoItemId = :pedidoItemId order by pr.id",
                        PedidoItemRetirada.class)
                .setParameter("pedidoItemId", pedidoItemId)
                .getResultList()
                .stream()
                .map(item -> new PedidoItemRetiradaDTO(
                        item.getId(),
                        item.getPedidoItemId(),
                        item.getIngredienteId(),
                        item.getIngredienteNome(),
                        item.getQuantidade(),
                        item.getCustoUnitario(),
                        item.getCustoTotal()
                ))
                .toList();
    }

    private List<ProdutoIngrediente> listarIngredientesProduto(Long produtoId) {
        return entityManager.createQuery(
                        "select pi from ProdutoIngrediente pi where pi.produtoId = :produtoId",
                        ProdutoIngrediente.class)
                .setParameter("produtoId", produtoId)
                .getResultList();
    }

    private List<Long> listarAdicionaisPermitidosIds(Long produtoId) {
        return entityManager.createQuery(
                        "select pa.adicionalId from ProdutoAdicional pa where pa.produtoId = :produtoId",
                        Long.class)
                .setParameter("produtoId", produtoId)
                .getResultList();
    }

    private List<AdicionalIngrediente> listarIngredientesAdicional(Long adicionalId) {
        return entityManager.createQuery(
                        "select ai from AdicionalIngrediente ai where ai.adicionalId = :adicionalId",
                        AdicionalIngrediente.class)
                .setParameter("adicionalId", adicionalId)
                .getResultList();
    }

    private String nomeIngrediente(Long ingredienteId) {
        Ingrediente ingrediente = entityManager.find(Ingrediente.class, ingredienteId);
        return ingrediente == null ? "Ingrediente #" + ingredienteId : ingrediente.getNome();
    }

    private double custoUnitarioIngrediente(Long ingredienteId) {
        return estoqueService.buscarPorIngrediente(ingredienteId).getCustoMedioAtual() == null
                ? 0d
                : estoqueService.buscarPorIngrediente(ingredienteId).getCustoMedioAtual();
    }

    private double valorEntrega(PedidoDTO dto) {
        return dto != null && dto.getTipoEntrega() == TipoEntrega.ENTREGA ? n(dto.getValorEntrega()) : 0d;
    }

    private void validarPedido(PedidoDTO dto) {
        if (dto == null || dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido precisa ter ao menos um item.");
        }
    }

    private double n(Double valor) {
        return valor == null ? 0d : valor;
    }

    private int n(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private Map<Long, Double> juntarConsumos(List<ItemMontado> itensMontados) {
        Map<Long, Double> consumos = new HashMap<>();
        for (ItemMontado montado : itensMontados) {
            for (Map.Entry<Long, Double> entry : montado.consumosIngredientes().entrySet()) {
                consumos.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        return consumos;
    }

    private record ItemMontado(PedidoItemDTO item,
                               Map<Long, Double> consumosIngredientes,
                               List<ConsumoMontado> consumos) {
    }

    private record ConsumoMontado(Long ingredienteId,
                                  String ingredienteNome,
                                  Double quantidade,
                                  Double custoUnitario,
                                  Double custoTotal,
                                  String origemTipo) {
    }
}
