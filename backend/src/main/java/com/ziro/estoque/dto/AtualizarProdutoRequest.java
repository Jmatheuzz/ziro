package com.ziro.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Nao inclui quantidadeEstoque de proposito - a quantidade so muda
 * atraves do endpoint de ajuste de estoque, pra manter claro que toda
 * mudanca de quantidade e uma entrada ou saida rastreavel, nao uma edicao solta.
 */
public record AtualizarProdutoRequest(

        @NotBlank(message = "Nome e obrigatorio")
        String nome,

        String descricao,

        @NotNull(message = "Preco de venda e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preco de venda precisa ser maior que zero")
        BigDecimal precoVenda,

        @DecimalMin(value = "0.0", message = "Preco de custo nao pode ser negativo")
        BigDecimal precoCusto,

        @Min(value = 0, message = "Estoque minimo nao pode ser negativo")
        Integer estoqueMinimo,

        String sku,

        UUID categoriaId
) {}
