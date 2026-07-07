package com.ziro.repository;

import com.ziro.model.ModuloConfiguracao;
import com.ziro.model.enums.TipoModulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModuloConfiguracaoRepository extends JpaRepository<ModuloConfiguracao, UUID> {
    List<ModuloConfiguracao> findByEmpresaId(UUID empresaId);
    Optional<ModuloConfiguracao> findByEmpresaIdAndModulo(UUID empresaId, TipoModulo modulo);
    boolean existsByEmpresaIdAndModuloAndAtivoTrue(UUID empresaId, TipoModulo modulo);
}
