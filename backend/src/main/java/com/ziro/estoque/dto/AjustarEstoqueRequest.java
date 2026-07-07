package com.ziro.estoque.dto;

import com.ziro.model.enums.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AjustarEstoqueRequest(

        @NotNull(message = "Tipo e obrigatorio")
        TipoMovimentacao tipo,

        @NotNull(message = "Quantidade e obrigatoria")
        @Positive(message = "Quantidade precisa ser maior que zero")
        Integer quantidade,

        String motivo
) {}
