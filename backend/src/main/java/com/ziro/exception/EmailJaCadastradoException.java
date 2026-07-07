package com.ziro.exception;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("Ja existe uma conta cadastrada com o email " + email);
    }
}
