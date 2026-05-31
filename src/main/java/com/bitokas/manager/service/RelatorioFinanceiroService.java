package com.bitokas.manager.service;

import com.bitokas.manager.dto.RelatorioFinanceiroDTO;
import com.bitokas.manager.model.gastos.CompraIngrediente;
import com.bitokas.manager.model.gastos.DespesaGeral;
import com.bitokas.manager.model.gastos.MovimentoEstoque;
import com.bitokas.manager.model.gastos.TipoMovimentoEstoque;
import com.bitokas.manager.model.pedidos.Pedido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
public class RelatorioFinanceiroService {

    @PersistenceContext
    private EntityManager entityManager;

    public Double calcularTotalVendas(LocalDateTime inicio, LocalDateTime fim) {
        return entityManager.createQuery(
                        "select p from Pedido p where p.dataHora >= :inicio and p.dataHora <= :fim",
                        Pedido.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList()
                .stream()
                .mapToDouble(p -> p.getValorPago() != null ? p.getValorPago() : n(p.getValorTotal()))
                .sum();
    }

    public Double calcularTotalCompras(LocalDateTime inicio, LocalDateTime fim) {
        Date inicioDate = toDate(inicio);
        Date fimDate = toDate(fim);

        return entityManager.createQuery(
                        "select c from CompraIngrediente c where c.dataCompra >= :inicio and c.dataCompra <= :fim",
                        CompraIngrediente.class)
                .setParameter("inicio", inicioDate)
                .setParameter("fim", fimDate)
                .getResultList()
                .stream()
                .mapToDouble(c -> n(c.getValorTotal()))
                .sum();
    }

    public Double calcularTotalDespesas(LocalDateTime inicio, LocalDateTime fim) {
        return entityManager.createQuery(
                        "select d from DespesaGeral d where d.dataDespesa >= :inicio and d.dataDespesa <= :fim",
                        DespesaGeral.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList()
                .stream()
                .mapToDouble(d -> n(d.getValor()))
                .sum();
    }

    public Double calcularCMV(LocalDateTime inicio, LocalDateTime fim) {
        return entityManager.createQuery(
                        "select m from MovimentoEstoque m where m.tipo = :tipo and m.dataHora between :inicio and :fim",
                        MovimentoEstoque.class)
                .setParameter("tipo", TipoMovimentoEstoque.SAIDA_PEDIDO)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList()
                .stream()
                .mapToDouble(m -> n(m.getValorTotal()))
                .sum();
    }

    public Double calcularLucroBruto(LocalDateTime inicio, LocalDateTime fim) {
        return calcularTotalVendas(inicio, fim) - calcularCMV(inicio, fim);
    }

    public Double calcularLucroLiquido(LocalDateTime inicio, LocalDateTime fim) {
        return calcularLucroBruto(inicio, fim) - calcularTotalDespesas(inicio, fim);
    }

    public RelatorioFinanceiroDTO gerarResumoFinanceiro(LocalDateTime inicio, LocalDateTime fim) {
        double vendas = calcularTotalVendas(inicio, fim);
        double compras = calcularTotalCompras(inicio, fim);
        double cmv = calcularCMV(inicio, fim);
        double despesas = calcularTotalDespesas(inicio, fim);
        double lucroBruto = vendas - cmv;
        double lucroLiquido = lucroBruto - despesas;

        return new RelatorioFinanceiroDTO(inicio, fim, vendas, compras, cmv, despesas, lucroBruto, lucroLiquido);
    }

    public List<Pedido> listarPedidosPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return entityManager.createQuery(
                        "select p from Pedido p where p.dataHora >= :inicio and p.dataHora <= :fim order by p.dataHora desc",
                        Pedido.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList();
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private double n(Double valor) {
        return valor == null ? 0d : valor;
    }
}
