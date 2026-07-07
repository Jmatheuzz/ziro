package com.ziro.financeiro;

import com.ziro.common.EmpresaAccessService;
import com.ziro.financeiro.dto.ResumoFinanceiroResponse;
import com.ziro.model.Empresa;
import com.ziro.model.enums.StatusConta;
import com.ziro.model.enums.TipoModulo;
import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.repository.financeiro.ContaPagarRepository;
import com.ziro.repository.financeiro.ContaReceberRepository;
import com.ziro.repository.financeiro.MovimentacaoCaixaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceiroResumoService {

    private final ContaPagarRepository contaPagarRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final EmpresaAccessService empresaAccessService;

    @Transactional(readOnly = true)
    public ResumoFinanceiroResponse resumo(UUID usuarioId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        UUID empresaId = empresa.getId();
        LocalDate hoje = LocalDate.now();

        var totalAPagar = contaPagarRepository.somarValorPorStatus(empresaId, StatusConta.ABERTA);
        var totalAReceber = contaReceberRepository.somarValorPorStatus(empresaId, StatusConta.ABERTA);

        LocalDate inicioMes = hoje.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fimMes = hoje.with(TemporalAdjusters.lastDayOfMonth());
        var entradasMes = movimentacaoCaixaRepository.somarPorTipoEPeriodo(empresaId, TipoMovimentacao.ENTRADA, inicioMes, fimMes);
        var saidasMes = movimentacaoCaixaRepository.somarPorTipoEPeriodo(empresaId, TipoMovimentacao.SAIDA, inicioMes, fimMes);

        long contasPagarVencidas = contaPagarRepository
                .countByEmpresaIdAndStatusAndDataVencimentoBefore(empresaId, StatusConta.ABERTA, hoje);
        long contasReceberVencidas = contaReceberRepository
                .countByEmpresaIdAndStatusAndDataVencimentoBefore(empresaId, StatusConta.ABERTA, hoje);

        return new ResumoFinanceiroResponse(
                totalAPagar,
                totalAReceber,
                entradasMes.subtract(saidasMes),
                contasPagarVencidas,
                contasReceberVencidas
        );
    }
}
