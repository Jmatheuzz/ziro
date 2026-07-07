package com.ziro.exception;

public class TokenInvalidoOuExpiradoException extends RuntimeException {
    public TokenInvalidoOuExpiradoException(String mensagem) {
        super(mensagem);
    }
}
