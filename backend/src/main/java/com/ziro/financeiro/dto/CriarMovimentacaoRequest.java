package com.ziro.financeiro.dto;

import com.ziro.model.enums.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarMovimentacaoRequest(

        @NotNull(message = "Tipo e obrigatorio")
        TipoMovimentacao tipo,

        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor precisa ser maior que zero")
        BigDecimal valor,

        @NotBlank(message = "Descricao e obrigatoria")
        String descricao,

        @NotNull(message = "Data e obrigatoria")
        LocalDate data
) {}
