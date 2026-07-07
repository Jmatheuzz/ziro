package com.ziro.venda.dto;

import java.math.BigDecimal;

public record VendasResumoResponse(
        BigDecimal totalVendidoMesAtual,
        long quantidadeVendasMesAtual,
        BigDecimal ticketMedioMesAtual
) {}
