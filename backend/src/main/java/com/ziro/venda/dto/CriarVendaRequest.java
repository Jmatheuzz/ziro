package com.ziro.venda.dto;

import com.ziro.model.enums.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CriarVendaRequest(

        UUID clienteId,

        LocalDate dataVenda,

        @NotNull(message = "Forma de pagamento e obrigatoria")
        FormaPagamento formaPagamento,

        @DecimalMin(value = "0.0", message = "Desconto nao pode ser negativo")
        BigDecimal desconto,

        String observacoes,

        @NotEmpty(message = "A venda precisa ter pelo menos um item")
        @Valid
        List<ItemVendaRequest> itens
) {}
