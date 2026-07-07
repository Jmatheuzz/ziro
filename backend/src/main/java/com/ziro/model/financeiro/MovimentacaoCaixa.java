package com.ziro.model.financeiro;

import com.ziro.model.base.TenantAwareEntity;
import com.ziro.model.enums.TipoMovimentacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro simples de fluxo de caixa. Pode ser gerado automaticamente
 * quando uma ContaPagar e paga ou uma ContaReceber e recebida,
 * ou lancado manualmente (ex: venda a vista, retirada do dono).
 */
@Getter
@Setter
@Entity
@Table(name = "movimentacao_caixa")
public class MovimentacaoCaixa extends TenantAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 50)
    private String origem; // ex: "CONTA_PAGAR", "CONTA_RECEBER", "MANUAL", "VENDA"
}
