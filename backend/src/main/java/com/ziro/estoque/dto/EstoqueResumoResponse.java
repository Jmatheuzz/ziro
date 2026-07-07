package com.ziro.estoque.dto;

import java.math.BigDecimal;

public record EstoqueResumoResponse(
        long totalProdutos,
        long produtosComEstoqueBaixo,
        BigDecimal valorTotalEstoque
) {}
