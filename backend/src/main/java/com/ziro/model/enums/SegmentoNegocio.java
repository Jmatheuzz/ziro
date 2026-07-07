package com.ziro.model.enums;

/**
 * Segmento do negocio, usado so pra sugerir uma configuracao inicial de
 * modulos no onboarding (ex: quem presta servico provavelmente nao precisa
 * de controle de estoque ligado por padrao). O dono pode mudar tudo depois.
 */
public enum SegmentoNegocio {
    COMERCIO,
    SERVICOS,
    ALIMENTACAO,
    OUTRO
}
