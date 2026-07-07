package com.ziro.financeiro.dto;

import java.math.BigDecimal;

public record ResumoFinanceiroResponse(
        BigDecimal totalAPagarEmAberto,
        BigDecimal totalAReceberEmAberto,
        BigDecimal saldoCaixaMesAtual,
        long contasPagarVencidas,
        long contasReceberVencidas
) {}
