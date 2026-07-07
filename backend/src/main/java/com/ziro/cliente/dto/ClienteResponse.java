package com.ziro.cliente.dto;

import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String telefone,
        String email,
        String cpfCnpj,
        String observacoes,
        boolean ativo
) {}
