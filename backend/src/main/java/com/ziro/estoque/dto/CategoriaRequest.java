package com.ziro.estoque.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "Nome e obrigatorio")
        String nome
) {}
