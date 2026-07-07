package com.ziro.repository;

import com.ziro.model.auth.TokenVerificacaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenVerificacaoEmailRepository extends JpaRepository<TokenVerificacaoEmail, UUID> {
    Optional<TokenVerificacaoEmail> findByCodigoAndUsadoFalse(String codigo);
}
