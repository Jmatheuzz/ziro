package com.ziro.financeiro;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.financeiro.dto.CriarMovimentacaoRequest;
import com.ziro.financeiro.dto.FluxoCaixaResponse;
import com.ziro.financeiro.dto.MovimentacaoCaixaResponse;
import com.ziro.model.Empresa;
import com.ziro.model.Usuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.model.financeiro.MovimentacaoCaixa;
import com.ziro.repository.financeiro.MovimentacaoCaixaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovimentacaoCaixaService {

    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final EmpresaAccessService empresaAccessService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public FluxoCaixaResponse listarFluxo(UUID usuarioId, LocalDate inicio, LocalDate fim) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);

        var movimentacoes = movimentacaoCaixaRepository
                .findByEmpresaIdAndDataBetweenOrderByDataDesc(empresa.getId(), inicio, fim)
                .stream()
                .map(this::paraResponse)
                .toList();

        BigDecimal totalEntradas = movimentacaoCaixaRepository
                .somarPorTipoEPeriodo(empresa.getId(), TipoMovimentacao.ENTRADA, inicio, fim);
        BigDecimal totalSaidas = movimentacaoCaixaRepository
                .somarPorTipoEPeriodo(empresa.getId(), TipoMovimentacao.SAIDA, inicio, fim);

        return new FluxoCaixaResponse(movimentacoes, totalEntradas, totalSaidas, totalEntradas.subtract(totalSaidas));
    }

    @Transactional
    public MovimentacaoCaixaResponse lancarManual(UUID usuarioId, CriarMovimentacaoRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();

        MovimentacaoCaixa movimentacao = new MovimentacaoCaixa();
        movimentacao.setEmpresa(empresa);
        movimentacao.setTipo(request.tipo());
        movimentacao.setValor(request.valor());
        movimentacao.setDescricao(request.descricao());
        movimentacao.setData(request.data());
        movimentacao.setOrigem("MANUAL");

        movimentacaoCaixaRepository.save(movimentacao);

        auditoriaService.registrar(empresa, usuario, "MOVIMENTACAO_CAIXA", movimentacao.getId(), "CRIACAO",
                "Lancamento manual de " + request.tipo().name().toLowerCase() + ": " + request.descricao());

        return paraResponse(movimentacao);
    }

    private MovimentacaoCaixaResponse paraResponse(MovimentacaoCaixa movimentacao) {
        return new MovimentacaoCaixaResponse(
                movimentacao.getId(),
                movimentacao.getTipo(),
                movimentacao.getValor(),
                movimentacao.getDescricao(),
                movimentacao.getData(),
                movimentacao.getOrigem()
        );
    }
}
