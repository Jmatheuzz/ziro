package com.ziro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> corpoErro(String mensagem, int status) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", status);
        corpo.put("mensagem", mensagem);
        return corpo;
    }

    @ExceptionHandler({EmailJaCadastradoException.class, EmpresaJaExisteException.class})
    public ResponseEntity<Map<String, Object>> handleConflito(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(corpoErro(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ResponseEntity<Map<String, Object>> handleOperacaoNaoPermitida(OperacaoNaoPermitidaException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(corpoErro(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(ModuloInativoException.class)
    public ResponseEntity<Map<String, Object>> handleModuloInativo(ModuloInativoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(corpoErro(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(PermissaoModuloNegadaException.class)
    public ResponseEntity<Map<String, Object>> handlePermissaoModuloNegada(PermissaoModuloNegadaException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(corpoErro(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler({CredenciaisInvalidasException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleCredenciaisInvalidas(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(corpoErro("Email ou senha invalidos", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(EmailNaoVerificadoException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNaoVerificado(EmailNaoVerificadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(corpoErro(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(TokenInvalidoOuExpiradoException.class)
    public ResponseEntity<Map<String, Object>> handleTokenInvalido(TokenInvalidoOuExpiradoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(corpoErro(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(corpoErro(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, Object> corpo = corpoErro("Dados invalidos", HttpStatus.BAD_REQUEST.value());
        Map<String, String> campos = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }
        corpo.put("campos", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerica(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(corpoErro("Erro interno inesperado", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
