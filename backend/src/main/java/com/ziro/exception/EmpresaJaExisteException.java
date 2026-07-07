package com.ziro.exception;

public class EmpresaJaExisteException extends RuntimeException {
    public EmpresaJaExisteException() {
        super("Esse usuario ja tem uma empresa cadastrada");
    }
}
