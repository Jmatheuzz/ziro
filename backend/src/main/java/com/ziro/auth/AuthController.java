package com.ziro.auth;

import com.ziro.auth.dto.EsqueciSenhaRequest;
import com.ziro.auth.dto.LoginRequest;
import com.ziro.auth.dto.MensagemResponse;
import com.ziro.auth.dto.RedefinirSenhaRequest;
import com.ziro.auth.dto.RefreshTokenRequest;
import com.ziro.auth.dto.RegisterRequest;
import com.ziro.auth.dto.TokenResponse;
import com.ziro.auth.dto.UsuarioAutenticadoResponse;
import com.ziro.auth.dto.VerificarEmailRequest;
import com.ziro.model.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registrar")
    public ResponseEntity<MensagemResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensagemResponse("Conta criada! Confira seu email pra confirmar o cadastro."));
    }

    @PostMapping("/verificar-email")
    public ResponseEntity<MensagemResponse> verificarEmail(@Valid @RequestBody VerificarEmailRequest request) {
        authService.verificarEmail(request);
        return ResponseEntity.ok(new MensagemResponse("Email verificado com sucesso! Ja pode fazer login."));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MensagemResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new MensagemResponse("Logout realizado com sucesso."));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        authService.esqueciSenha(request);
        return ResponseEntity.ok(new MensagemResponse(
                "Se esse email estiver cadastrado, voce vai receber um link de recuperacao."));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<MensagemResponse> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        authService.redefinirSenha(request);
        return ResponseEntity.ok(new MensagemResponse("Senha redefinida com sucesso! Ja pode fazer login."));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioAutenticadoResponse> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(new UsuarioAutenticadoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole().name(),
                usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null
        ));
    }
}
