package com.ziro.repository;

import com.ziro.model.RegistroAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, UUID> {

    Page<RegistroAuditoria> findByEmpresaIdOrderByCriadoEmDesc(UUID empresaId, Pageable pageable);

    Page<RegistroAuditoria> findByEmpresaIdAndEntidadeOrderByCriadoEmDesc(UUID empresaId, String entidade, Pageable pageable);
}
