package com.ziro.estoque;

import com.ziro.estoque.dto.EstoqueResumoResponse;
import com.ziro.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estoque/resumo")
@RequiredArgsConstructor
public class EstoqueResumoController {

    private final EstoqueResumoService estoqueResumoService;

    @GetMapping
    public ResponseEntity<EstoqueResumoResponse> resumo(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(estoqueResumoService.resumo(usuario.getId()));
    }
}
