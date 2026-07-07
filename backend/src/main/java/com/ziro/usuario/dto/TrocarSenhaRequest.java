package com.ziro.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocarSenhaRequest(
        @NotBlank(message = "Senha atual e obrigatoria")
        String senhaAtual,

        @NotBlank(message = "Nova senha e obrigatoria")
        @Size(min = 8, message = "Nova senha precisa ter no minimo 8 caracteres")
        String novaSenha
) {}
