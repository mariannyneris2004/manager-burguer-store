package com.bitokas.manager.service;

import com.bitokas.manager.dto.RelatorioFinanceiroDTO;
import com.bitokas.manager.model.gastos.CompraIngrediente;
import com.bitokas.manager.model.gastos.DespesaGeral;
import com.bitokas.manager.model.pedidos.Pedido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class RelatorioFinanceiroService {

    @PersistenceContext
    private EntityManager entityManager;

    public Double calcularTotalVendas(LocalDateTime inicio, LocalDateTime fim) {
        return entityManager.createQuery(
                        "select p from Pedido p where p.dataHora between :inicio and :fim",
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
                        "select c from CompraIngrediente c where c.dataCompra between :inicio and :fim",
                        CompraIngrediente.class)
                .setParameter("inicio", inicioDate)
                .setParameter("fim", fimDate)
                .getResultList()
                .stream()
                .mapToDouble(c -> n(c.getValorTotal()))
                .sum();
    }

    public Double calcularTotalDespesas(LocalDateTime inicio, LocalDateTime fim) {
        Date inicioDate = toDate(inicio);
        Date fimDate = toDate(fim);

        return entityManager.createQuery(
                        "select d from DespesaGeral d where d.dataDespesa between :inicio and :fim",
                        DespesaGeral.class)
                .setParameter("inicio", inicioDate)
                .setParameter("fim", fimDate)
                .getResultList()
                .stream()
                .mapToDouble(d -> n(d.getValor()))
                .sum();
    }

    public Double calcularLucro(LocalDateTime inicio, LocalDateTime fim) {
        double vendas = calcularTotalVendas(inicio, fim);
        double compras = calcularTotalCompras(inicio, fim);
        double despesas = calcularTotalDespesas(inicio, fim);
        return vendas - compras - despesas;
    }

    public RelatorioFinanceiroDTO gerarResumoFinanceiro(LocalDateTime inicio, LocalDateTime fim) {
        double vendas = calcularTotalVendas(inicio, fim);
        double compras = calcularTotalCompras(inicio, fim);
        double despesas = calcularTotalDespesas(inicio, fim);
        double lucro = vendas - compras - despesas;

        return new RelatorioFinanceiroDTO(inicio, fim, vendas, compras, despesas, lucro);
    }

    public List<Pedido> listarPedidosPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return entityManager.createQuery(
                        "select p from Pedido p where p.dataHora between :inicio and :fim order by p.dataHora desc",
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