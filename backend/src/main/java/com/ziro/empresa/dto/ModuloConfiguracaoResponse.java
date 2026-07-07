package com.ziro.empresa.dto;

import com.ziro.model.enums.TipoModulo;

import java.util.UUID;

public record ModuloConfiguracaoResponse(
        UUID id,
        TipoModulo modulo,
        boolean ativo,
        String configuracaoJson
) {}
