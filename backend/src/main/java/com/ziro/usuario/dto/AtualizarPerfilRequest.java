package com.ziro.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarPerfilRequest(
        @NotBlank(message = "Nome e obrigatorio")
        String nome
) {}
