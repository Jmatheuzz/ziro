package com.ziro.repository.financeiro;

import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.model.financeiro.MovimentacaoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MovimentacaoCaixaRepository extends JpaRepository<MovimentacaoCaixa, UUID> {

    List<MovimentacaoCaixa> findByEmpresaIdAndDataBetweenOrderByDataDesc(UUID empresaId, LocalDate inicio, LocalDate fim);

    @Query("select coalesce(sum(m.valor), 0) from MovimentacaoCaixa m " +
            "where m.empresa.id = :empresaId and m.tipo = :tipo and m.data between :inicio and :fim")
    BigDecimal somarPorTipoEPeriodo(@Param("empresaId") UUID empresaId,
                                     @Param("tipo") TipoMovimentacao tipo,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);
}
