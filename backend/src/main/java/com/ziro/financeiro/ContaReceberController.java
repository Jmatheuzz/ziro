package com.ziro.financeiro;

import com.ziro.financeiro.dto.AtualizarContaReceberRequest;
import com.ziro.financeiro.dto.ContaReceberResponse;
import com.ziro.financeiro.dto.CriarContaReceberRequest;
import com.ziro.financeiro.dto.MarcarRecebidaRequest;
import com.ziro.model.Usuario;
import com.ziro.model.enums.StatusConta;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/financeiro/contas-receber")
@RequiredArgsConstructor
public class ContaReceberController {

    private final ContaReceberService contaReceberService;

    @GetMapping
    public ResponseEntity<Page<ContaReceberResponse>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) StatusConta status,
            @PageableDefault(size = 20, sort = "dataVencimento") Pageable pageable) {
        return ResponseEntity.ok(contaReceberService.listar(usuario.getId(), status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaReceberResponse> buscar(@AuthenticationPrincipal Usuario usuario,
                                                        @PathVariable UUID id) {
        return ResponseEntity.ok(contaReceberService.buscarPorId(usuario.getId(), id));
    }

    @PostMapping
    public ResponseEntity<ContaReceberResponse> criar(@AuthenticationPrincipal Usuario usuario,
                                                       @Valid @RequestBody CriarContaReceberRequest request) {
        ContaReceberResponse resposta = contaReceberService.criar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaReceberResponse> atualizar(@AuthenticationPrincipal Usuario usuario,
                                                           @PathVariable UUID id,
                                                           @Valid @RequestBody AtualizarContaReceberRequest request) {
        return ResponseEntity.ok(contaReceberService.atualizar(usuario.getId(), id, request));
    }

    @PatchMapping("/{id}/receber")
    public ResponseEntity<ContaReceberResponse> marcarComoRecebida(@AuthenticationPrincipal Usuario usuario,
                                                                    @PathVariable UUID id,
                                                                    @RequestBody(required = false) MarcarRecebidaRequest request) {
        MarcarRecebidaRequest corpo = request != null ? request : new MarcarRecebidaRequest(null);
        return ResponseEntity.ok(contaReceberService.marcarComoRecebida(usuario.getId(), id, corpo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID id) {
        contaReceberService.cancelar(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
