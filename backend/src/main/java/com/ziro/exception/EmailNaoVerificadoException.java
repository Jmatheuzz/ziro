package com.ziro.exception;

public class EmailNaoVerificadoException extends RuntimeException {
    public EmailNaoVerificadoException() {
        super("Email ainda nao foi verificado. Confira sua caixa de entrada");
    }
}
