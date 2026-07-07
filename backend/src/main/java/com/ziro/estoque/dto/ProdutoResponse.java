package com.ziro.estoque.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        String nome,
        String descricao,
        BigDecimal precoVenda,
        BigDecimal precoCusto,
        Integer quantidadeEstoque,
        Integer estoqueMinimo,
        String sku,
        UUID categoriaId,
        String categoriaNome,
        boolean ativo,
        boolean estoqueBaixo
) {}
