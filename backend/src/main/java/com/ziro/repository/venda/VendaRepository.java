package com.ziro.repository.venda;

import com.ziro.model.enums.StatusVenda;
import com.ziro.model.venda.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<Venda, UUID> {

    Page<Venda> findByEmpresaIdOrderByDataVendaDescCriadoEmDesc(UUID empresaId, Pageable pageable);

    Page<Venda> findByEmpresaIdAndStatusOrderByDataVendaDescCriadoEmDesc(UUID empresaId, StatusVenda status, Pageable pageable);

    @Query("select coalesce(sum(v.valorTotal), 0) from Venda v " +
            "where v.empresa.id = :empresaId and v.status = :status " +
            "and v.dataVenda between :inicio and :fim")
    BigDecimal somarValorPorPeriodo(@Param("empresaId") UUID empresaId,
                                     @Param("status") StatusVenda status,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);

    long countByEmpresaIdAndStatusAndDataVendaBetween(UUID empresaId, StatusVenda status, LocalDate inicio, LocalDate fim);
}
