package com.ziro.repository;

import com.ziro.model.Usuario;
import com.ziro.model.enums.RoleUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByEmpresaIdAndRole(UUID empresaId, RoleUsuario role);
}
