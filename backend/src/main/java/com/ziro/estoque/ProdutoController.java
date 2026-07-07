package com.ziro.estoque;

import com.ziro.estoque.dto.AjustarEstoqueRequest;
import com.ziro.estoque.dto.AtualizarProdutoRequest;
import com.ziro.estoque.dto.CriarProdutoRequest;
import com.ziro.estoque.dto.ProdutoResponse;
import com.ziro.model.Usuario;
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
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<Page<ProdutoResponse>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) UUID categoriaId,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(produtoService.listar(usuario.getId(), busca, categoriaId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscar(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID id) {
        return ResponseEntity.ok(produtoService.buscarPorId(usuario.getId(), id));
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@AuthenticationPrincipal Usuario usuario,
                                                  @Valid @RequestBody CriarProdutoRequest request) {
        ProdutoResponse resposta = produtoService.criar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@AuthenticationPrincipal Usuario usuario,
                                                      @PathVariable UUID id,
                                                      @Valid @RequestBody AtualizarProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(usuario.getId(), id, request));
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<ProdutoResponse> ajustarEstoque(@AuthenticationPrincipal Usuario usuario,
                                                           @PathVariable UUID id,
                                                           @Valid @RequestBody AjustarEstoqueRequest request) {
        return ResponseEntity.ok(produtoService.ajustarEstoque(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID id) {
        produtoService.excluir(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
