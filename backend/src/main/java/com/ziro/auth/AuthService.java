package com.ziro.auth;

import com.ziro.auth.dto.EsqueciSenhaRequest;
import com.ziro.auth.dto.LoginRequest;
import com.ziro.auth.dto.RedefinirSenhaRequest;
import com.ziro.auth.dto.RefreshTokenRequest;
import com.ziro.auth.dto.RegisterRequest;
import com.ziro.auth.dto.TokenResponse;
import com.ziro.auth.dto.VerificarEmailRequest;
import com.ziro.email.EmailService;
import com.ziro.exception.CredenciaisInvalidasException;
import com.ziro.exception.EmailJaCadastradoException;
import com.ziro.exception.EmailNaoVerificadoException;
import com.ziro.exception.TokenInvalidoOuExpiradoException;
import com.ziro.model.Usuario;
import com.ziro.model.auth.RefreshToken;
import com.ziro.model.auth.TokenRecuperacaoSenha;
import com.ziro.model.auth.TokenVerificacaoEmail;
import com.ziro.model.enums.RoleUsuario;
import com.ziro.model.enums.StatusUsuario;
import com.ziro.repository.RefreshTokenRepository;
import com.ziro.repository.TokenRecuperacaoSenhaRepository;
import com.ziro.repository.TokenVerificacaoEmailRepository;
import com.ziro.repository.UsuarioRepository;
import com.ziro.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long EXPIRACAO_TOKEN_EMAIL_HORAS = 24;
    private static final long EXPIRACAO_TOKEN_SENHA_HORAS = 2;
    private static final long EXPIRACAO_REFRESH_TOKEN_DIAS = 7;

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenVerificacaoEmailRepository tokenVerificacaoEmailRepository;
    private final TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Transactional
    public void registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setRole(RoleUsuario.ADMIN); // quem se auto-registra e o dono/admin da futura empresa
        usuario.setStatus(StatusUsuario.PENDENTE_VERIFICACAO);
        usuarioRepository.save(usuario);

        criarEEnviarTokenVerificacao(usuario);
    }

    private void criarEEnviarTokenVerificacao(Usuario usuario) {
        TokenVerificacaoEmail token = new TokenVerificacaoEmail();
        token.setCodigo(UUID.randomUUID().toString());
        token.setUsuario(usuario);
        token.setExpiraEm(LocalDateTime.now().plusHours(EXPIRACAO_TOKEN_EMAIL_HORAS));
        tokenVerificacaoEmailRepository.save(token);

        emailService.enviarEmailVerificacao(usuario.getEmail(), usuario.getNome(), token.getCodigo());
    }

    @Transactional
    public void verificarEmail(VerificarEmailRequest request) {
        TokenVerificacaoEmail token = tokenVerificacaoEmailRepository.findByCodigoAndUsadoFalse(request.codigo())
                .orElseThrow(() -> new TokenInvalidoOuExpiradoException("Codigo de verificacao invalido ou ja usado"));

        if (token.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoOuExpiradoException("Codigo de verificacao expirado, solicite um novo");
        }

        Usuario usuario = token.getUsuario();
        usuario.setStatus(StatusUsuario.ATIVO);
        usuarioRepository.save(usuario);

        token.setUsado(true);
        tokenVerificacaoEmailRepository.save(token);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        if (usuario.getStatus() == StatusUsuario.PENDENTE_VERIFICACAO) {
            throw new EmailNaoVerificadoException();
        }

        if (usuario.getStatus() == StatusUsuario.INATIVO) {
            throw new CredenciaisInvalidasException();
        }

        return gerarTokens(usuario);
    }

    private TokenResponse gerarTokens(Usuario usuario) {
        String accessToken = jwtService.gerarAccessToken(usuario);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsuario(usuario);
        refreshToken.setExpiraEm(LocalDateTime.now().plusDays(EXPIRACAO_REFRESH_TOKEN_DIAS));
        refreshTokenRepository.save(refreshToken);

        long expiraEmSegundos = jwtService.getAccessTokenExpiracaoMinutos() * 60;
        return new TokenResponse(accessToken, refreshToken.getToken(), expiraEmSegundos);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken tokenAtual = refreshTokenRepository.findByTokenAndRevogadoFalse(request.refreshToken())
                .orElseThrow(() -> new TokenInvalidoOuExpiradoException("Refresh token invalido"));

        if (tokenAtual.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoOuExpiradoException("Refresh token expirado, faca login novamente");
        }

        // rotaciona o refresh token: revoga o atual e emite um novo par
        tokenAtual.setRevogado(true);
        refreshTokenRepository.save(tokenAtual);

        return gerarTokens(tokenAtual.getUsuario());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenAndRevogadoFalse(request.refreshToken())
                .ifPresent(token -> {
                    token.setRevogado(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void esqueciSenha(EsqueciSenhaRequest request) {
        // nao revela se o email existe ou nao, pra nao dar pista pra quem tenta enumerar contas
        usuarioRepository.findByEmail(request.email()).ifPresent(usuario -> {
            TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
            token.setCodigo(UUID.randomUUID().toString());
            token.setUsuario(usuario);
            token.setExpiraEm(LocalDateTime.now().plusHours(EXPIRACAO_TOKEN_SENHA_HORAS));
            tokenRecuperacaoSenhaRepository.save(token);

            emailService.enviarEmailRecuperacaoSenha(usuario.getEmail(), usuario.getNome(), token.getCodigo());
        });
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        TokenRecuperacaoSenha token = tokenRecuperacaoSenhaRepository.findByCodigoAndUsadoFalse(request.codigo())
                .orElseThrow(() -> new TokenInvalidoOuExpiradoException("Codigo invalido ou ja usado"));

        if (token.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoOuExpiradoException("Codigo expirado, solicite a recuperacao novamente");
        }

        Usuario usuario = token.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);

        token.setUsado(true);
        tokenRecuperacaoSenhaRepository.save(token);
    }
}
