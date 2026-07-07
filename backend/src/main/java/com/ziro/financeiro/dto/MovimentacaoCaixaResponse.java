package com.ziro.financeiro.dto;

import com.ziro.model.enums.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MovimentacaoCaixaResponse(
        UUID id,
        TipoMovimentacao tipo,
        BigDecimal valor,
        String descricao,
        LocalDate data,
        String origem
) {}
