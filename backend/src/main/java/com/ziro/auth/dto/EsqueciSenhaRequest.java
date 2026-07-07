package com.ziro.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(
        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        String email
) {}
