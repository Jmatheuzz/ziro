package com.ziro.repository;

import com.ziro.model.UsuarioModuloPermissao;
import com.ziro.model.enums.TipoModulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsuarioModuloPermissaoRepository extends JpaRepository<UsuarioModuloPermissao, UUID> {

    boolean existsByUsuarioIdAndModulo(UUID usuarioId, TipoModulo modulo);

    List<UsuarioModuloPermissao> findByUsuarioId(UUID usuarioId);

    void deleteByUsuarioId(UUID usuarioId);
}
