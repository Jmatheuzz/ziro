package com.ziro.financeiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CriarContaReceberRequest(

        @NotBlank(message = "Descricao e obrigatoria")
        String descricao,

        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor precisa ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "Data de vencimento e obrigatoria")
        LocalDate dataVencimento,

        UUID clienteId
) {}
