package com.ziro.financeiro.dto;

import com.ziro.model.enums.StatusConta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContaReceberResponse(
        UUID id,
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        LocalDate dataRecebimento,
        UUID clienteId,
        String clienteNome,
        StatusConta status,
        boolean atrasada
) {}
