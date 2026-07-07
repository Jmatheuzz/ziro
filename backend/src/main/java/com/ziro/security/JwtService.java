package com.ziro.security;

import com.ziro.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${ziro.jwt.secret}")
    private String secret;

    @Value("${ziro.jwt.access-token-expiracao-minutos}")
    private long accessTokenExpiracaoMinutos;

    private SecretKey chaveAssinatura() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarAccessToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(accessTokenExpiracaoMinutos, ChronoUnit.MINUTES);

        var builder = Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nome", usuario.getNome())
                .claim("role", usuario.getRole().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao));

        if (usuario.getEmpresa() != null) {
            builder.claim("empresaId", usuario.getEmpresa().getId().toString());
        }

        return builder.signWith(chaveAssinatura()).compact();
    }

    public long getAccessTokenExpiracaoMinutos() {
        return accessTokenExpiracaoMinutos;
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chaveAssinatura())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extrairUsuarioId(String token) {
        return UUID.fromString(extrairClaims(token).getSubject());
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = extrairClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
