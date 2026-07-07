package com.ziro.financeiro.dto;

import java.time.LocalDate;

/** dataRecebimento nulo significa "hoje" */
public record MarcarRecebidaRequest(LocalDate dataRecebimento) {}
