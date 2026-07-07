package com.ziro.exception;

import com.ziro.model.enums.TipoModulo;

public class PermissaoModuloNegadaException extends RuntimeException {

    public PermissaoModuloNegadaException(TipoModulo modulo) {
        super("Voce nao tem permissao pra acessar o modulo " + nomeAmigavel(modulo)
                + ". Fale com o administrador da conta.");
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
