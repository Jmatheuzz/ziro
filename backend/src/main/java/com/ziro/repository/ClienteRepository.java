package com.ziro.repository;

import com.ziro.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Page<Cliente> findByEmpresaIdAndAtivoTrue(UUID empresaId, Pageable pageable);
    Page<Cliente> findByEmpresaIdAndAtivoTrueAndNomeContainingIgnoreCase(UUID empresaId, String nome, Pageable pageable);
}
