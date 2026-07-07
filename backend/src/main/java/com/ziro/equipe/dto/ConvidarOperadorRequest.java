package com.ziro.equipe.dto;

import com.ziro.model.enums.TipoModulo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record ConvidarOperadorRequest(

        @NotBlank(message = "Nome e obrigatorio")
        String nome,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @NotEmpty(message = "Escolha pelo menos um modulo pra esse operador acessar")
        Set<TipoModulo> modulos
) {}
