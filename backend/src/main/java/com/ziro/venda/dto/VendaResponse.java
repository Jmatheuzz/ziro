package com.ziro.venda.dto;

import com.ziro.model.enums.FormaPagamento;
import com.ziro.model.enums.StatusVenda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VendaResponse(
        UUID id,
        UUID clienteId,
        String clienteNome,
        LocalDate dataVenda,
        StatusVenda status,
        FormaPagamento formaPagamento,
        BigDecimal valorTotal,
        BigDecimal desconto,
        String observacoes,
        List<ItemVendaResponse> itens
) {}
