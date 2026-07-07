package com.ziro.empresa.dto;

import com.ziro.model.enums.SegmentoNegocio;

import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        String nomeFantasia,
        String razaoSocial,
        String cnpjCpf,
        SegmentoNegocio segmento,
        boolean ativa
) {}
