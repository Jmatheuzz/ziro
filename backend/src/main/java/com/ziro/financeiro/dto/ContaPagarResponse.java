package com.ziro.financeiro.dto;

import com.ziro.model.enums.StatusConta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContaPagarResponse(
        UUID id,
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        String fornecedor,
        String categoria,
        StatusConta status,
        boolean atrasada
) {}
