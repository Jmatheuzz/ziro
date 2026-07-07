package com.ziro.financeiro.dto;

import java.math.BigDecimal;
import java.util.List;

public record FluxoCaixaResponse(
        List<MovimentacaoCaixaResponse> movimentacoes,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldo
) {}
