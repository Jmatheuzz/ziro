package com.ziro.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerificarEmailRequest(
        @NotBlank(message = "codigo e obrigatorio")
        String codigo
) {}
