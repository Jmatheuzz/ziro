package com.ziro.auditoria.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroAuditoriaResponse(
        UUID id,
        String usuarioNome,
        String entidade,
        UUID entidadeId,
        String acao,
        String descricao,
        LocalDateTime criadoEm
) {}
