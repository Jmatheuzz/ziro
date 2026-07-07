package com.ziro.exception;

import com.ziro.model.enums.TipoModulo;

public class ModuloInativoException extends RuntimeException {

    public ModuloInativoException(TipoModulo modulo) {
        super("O modulo " + nomeAmigavel(modulo) + " nao esta ativo pra sua empresa. "
                + "Ative em Configuracoes > Personalizacao pra continuar.");
    }

    private static String nomeAmigavel(TipoModulo modulo) {
        return switch (modulo) {
            case FINANCEIRO -> "Financeiro";
            case ESTOQUE -> "Estoque";
            case CLIENTES -> "Clientes";
            case VENDAS -> "Vendas";
        };
    }
}
