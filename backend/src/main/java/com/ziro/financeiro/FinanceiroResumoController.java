package com.ziro.financeiro;

import com.ziro.financeiro.dto.ResumoFinanceiroResponse;
import com.ziro.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financeiro/resumo")
@RequiredArgsConstructor
public class FinanceiroResumoController {

    private final FinanceiroResumoService financeiroResumoService;

    @GetMapping
    public ResponseEntity<ResumoFinanceiroResponse> resumo(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(financeiroResumoService.resumo(usuario.getId()));
    }
}
