package com.ziro.model.enums;

/**
 * FIADO gera uma ContaReceber (fica pra cobrar depois).
 * As outras geram uma MovimentacaoCaixa de ENTRADA na hora (dinheiro ja entrou).
 */
public enum FormaPagamento {
    DINHEIRO,
    CARTAO,
    PIX,
    FIADO
}
