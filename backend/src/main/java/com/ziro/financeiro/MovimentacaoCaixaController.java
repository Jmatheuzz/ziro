package com.ziro.financeiro;

import com.ziro.financeiro.dto.CriarMovimentacaoRequest;
import com.ziro.financeiro.dto.FluxoCaixaResponse;
import com.ziro.financeiro.dto.MovimentacaoCaixaResponse;
import com.ziro.model.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@RestController
@RequestMapping("/api/financeiro/fluxo-caixa")
@RequiredArgsConstructor
public class MovimentacaoCaixaController {

    private final MovimentacaoCaixaService movimentacaoCaixaService;

    @GetMapping
    public ResponseEntity<FluxoCaixaResponse> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        LocalDate hoje = LocalDate.now();
        LocalDate dataInicio = inicio != null ? inicio : hoje.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate dataFim = fim != null ? fim : hoje.with(TemporalAdjusters.lastDayOfMonth());

        return ResponseEntity.ok(movimentacaoCaixaService.listarFluxo(usuario.getId(), dataInicio, dataFim));
    }

    @PostMapping
    public ResponseEntity<MovimentacaoCaixaResponse> lancarManual(@AuthenticationPrincipal Usuario usuario,
                                                                   @Valid @RequestBody CriarMovimentacaoRequest request) {
        MovimentacaoCaixaResponse resposta = movimentacaoCaixaService.lancarManual(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
}
