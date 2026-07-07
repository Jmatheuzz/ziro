package com.ziro.repository.financeiro;

import com.ziro.model.enums.StatusConta;
import com.ziro.model.financeiro.ContaReceber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, UUID> {

    Page<ContaReceber> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<ContaReceber> findByEmpresaIdAndStatus(UUID empresaId, StatusConta status, Pageable pageable);

    @Query("select coalesce(sum(c.valor), 0) from ContaReceber c where c.empresa.id = :empresaId and c.status = :status")
    BigDecimal somarValorPorStatus(@Param("empresaId") UUID empresaId, @Param("status") StatusConta status);

    long countByEmpresaIdAndStatusAndDataVencimentoBefore(UUID empresaId, StatusConta status, LocalDate data);
}
