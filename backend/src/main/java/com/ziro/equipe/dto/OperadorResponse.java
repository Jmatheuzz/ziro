package com.ziro.equipe.dto;

import com.ziro.model.enums.StatusUsuario;
import com.ziro.model.enums.TipoModulo;

import java.util.Set;
import java.util.UUID;

public record OperadorResponse(
        UUID id,
        String nome,
        String email,
        StatusUsuario status,
        Set<TipoModulo> modulos
) {}
