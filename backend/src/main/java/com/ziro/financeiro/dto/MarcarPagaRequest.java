package com.ziro.financeiro.dto;

import java.time.LocalDate;

/** dataPagamento nulo significa "hoje" */
public record MarcarPagaRequest(LocalDate dataPagamento) {}
