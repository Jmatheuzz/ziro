package com.ziro.venda;

import com.ziro.model.Usuario;
import com.ziro.model.enums.StatusVenda;
import com.ziro.venda.dto.CriarVendaRequest;
import com.ziro.venda.dto.VendaResponse;
import com.ziro.venda.dto.VendasResumoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @GetMapping
    public ResponseEntity<Page<VendaResponse>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) StatusVenda status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(vendaService.listar(usuario.getId(), status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponse> buscar(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID id) {
        return ResponseEntity.ok(vendaService.buscarPorId(usuario.getId(), id));
    }

    @PostMapping
    public ResponseEntity<VendaResponse> criar(@AuthenticationPrincipal Usuario usuario,
                                                @Valid @RequestBody CriarVendaRequest request) {
        VendaResponse resposta = vendaService.criar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID id) {
        vendaService.cancelar(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resumo")
    public ResponseEntity<VendasResumoResponse> resumo(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(vendaService.resumo(usuario.getId()));
    }
}
