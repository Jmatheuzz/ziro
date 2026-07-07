package com.ziro.auth.dto;

import java.util.UUID;

public record UsuarioAutenticadoResponse(
        UUID id,
        String nome,
        String email,
        String role,
        UUID empresaId
) {}
