package com.ziro.estoque.dto;

import java.util.UUID;

public record CategoriaResponse(
        UUID id,
        String nome
) {}
