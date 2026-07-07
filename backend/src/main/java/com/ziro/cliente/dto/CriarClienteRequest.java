package com.ziro.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CriarClienteRequest(

        @NotBlank(message = "Nome e obrigatorio")
        String nome,

        String telefone,

        @Email(message = "Email invalido")
        String email,

        String cpfCnpj,

        String observacoes
) {}
