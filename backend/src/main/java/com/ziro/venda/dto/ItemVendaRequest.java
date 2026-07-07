package com.ziro.venda.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ItemVendaRequest(

        @NotNull(message = "Produto e obrigatorio")
        UUID produtoId,

        @NotNull(message = "Quantidade e obrigatoria")
        @Positive(message = "Quantidade precisa ser maior que zero")
        Integer quantidade
) {}
