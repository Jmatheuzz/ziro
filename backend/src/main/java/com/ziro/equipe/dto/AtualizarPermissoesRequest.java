package com.ziro.equipe.dto;

import com.ziro.model.enums.TipoModulo;

import java.util.Set;

/** Set vazio e valido - significa "revogar todos os acessos a modulo desse operador". */
public record AtualizarPermissoesRequest(
        Set<TipoModulo> modulos
) {}
