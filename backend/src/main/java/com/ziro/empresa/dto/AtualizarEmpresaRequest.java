package com.ziro.empresa.dto;

import com.ziro.model.enums.SegmentoNegocio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarEmpresaRequest(

        @NotBlank(message = "Nome fantasia e obrigatorio")
        String nomeFantasia,

        String razaoSocial,

        String cnpjCpf,

        @NotNull(message = "Segmento e obrigatorio")
        SegmentoNegocio segmento
) {}
