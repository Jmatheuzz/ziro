package com.ziro.model.venda;

import com.ziro.model.Cliente;
import com.ziro.model.base.TenantAwareEntity;
import com.ziro.model.enums.FormaPagamento;
import com.ziro.model.enums.StatusVenda;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Venda registra a transacao em si. Quando finalizada (criada), ja da baixa
 * no estoque dos produtos e gera a consequencia financeira (ContaReceber se
 * for fiado, ou uma MovimentacaoCaixa de entrada se for pagamento a vista).
 * Cancelar uma venda reverte as duas coisas.
 */
@Getter
@Setter
@Entity
@Table(name = "venda")
public class Venda extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "data_venda", nullable = false)
    private LocalDate dataVenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVenda status = StatusVenda.ATIVA;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(length = 500)
    private String observacoes;

    /** Referencia simples (sem relacao JPA) pra saber o que reverter se a venda for cancelada. */
    @Column(name = "conta_receber_id")
    private UUID contaReceberId;

    @Column(name = "movimentacao_caixa_id")
    private UUID movimentacaoCaixaId;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("criadoEm asc")
    private List<ItemVenda> itens = new ArrayList<>();
}
