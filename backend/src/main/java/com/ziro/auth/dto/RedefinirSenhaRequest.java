package com.ziro.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "codigo e obrigatorio")
        String codigo,

        @NotBlank(message = "Nova senha e obrigatoria")
        @Size(min = 8, message = "Senha precisa ter no minimo 8 caracteres")
        String novaSenha
) {}
