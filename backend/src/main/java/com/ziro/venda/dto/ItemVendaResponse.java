package com.ziro.venda.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemVendaResponse(
        UUID produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {}
