package com.ziro.estoque;

import com.ziro.estoque.dto.CategoriaRequest;
import com.ziro.estoque.dto.CategoriaResponse;
import com.ziro.model.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(categoriaService.listar(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@AuthenticationPrincipal Usuario usuario,
                                                    @Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse resposta = categoriaService.criar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID id) {
        categoriaService.excluir(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
