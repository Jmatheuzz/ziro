package com.ziro.financeiro;

import com.ziro.financeiro.dto.AtualizarContaPagarRequest;
import com.ziro.financeiro.dto.ContaPagarResponse;
import com.ziro.financeiro.dto.CriarContaPagarRequest;
import com.ziro.financeiro.dto.MarcarPagaRequest;
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
@RequestMapping("/api/financeiro/contas-pagar")
@RequiredArgsConstructor
public class ContaPagarController {

    private final ContaPagarService contaPagarService;

    @GetMapping
    public ResponseEntity<Page<ContaPagarResponse>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) StatusConta status,
            @PageableDefault(size = 20, sort = "dataVencimento") Pageable pageable) {
        return ResponseEntity.ok(contaPagarService.listar(usuario.getId(), status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaPagarResponse> buscar(@AuthenticationPrincipal Usuario usuario,
                                                      @PathVariable UUID id) {
        return ResponseEntity.ok(contaPagarService.buscarPorId(usuario.getId(), id));
    }

    @PostMapping
    public ResponseEntity<ContaPagarResponse> criar(@AuthenticationPrincipal Usuario usuario,
                                                     @Valid @RequestBody CriarContaPagarRequest request) {
        ContaPagarResponse resposta = contaPagarService.criar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaPagarResponse> atualizar(@AuthenticationPrincipal Usuario usuario,
                                                         @PathVariable UUID id,
                                                         @Valid @RequestBody AtualizarContaPagarRequest request) {
        return ResponseEntity.ok(contaPagarService.atualizar(usuario.getId(), id, request));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ContaPagarResponse> marcarComoPaga(@AuthenticationPrincipal Usuario usuario,
                                                              @PathVariable UUID id,
                                                              @RequestBody(required = false) MarcarPagaRequest request) {
        MarcarPagaRequest corpo = request != null ? request : new MarcarPagaRequest(null);
        return ResponseEntity.ok(contaPagarService.marcarComoPaga(usuario.getId(), id, corpo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID id) {
        contaPagarService.cancelar(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
