package com.ziro.empresa;

import com.ziro.empresa.dto.AtualizarModuloRequest;
import com.ziro.empresa.dto.ModuloConfiguracaoResponse;
import com.ziro.model.Usuario;
import com.ziro.model.enums.TipoModulo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/modulos")
@RequiredArgsConstructor
public class ModuloConfiguracaoController {

    private final ModuloConfiguracaoService moduloConfiguracaoService;

    @GetMapping
    public ResponseEntity<List<ModuloConfiguracaoResponse>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(moduloConfiguracaoService.listar(usuario.getId()));
    }

    @PatchMapping("/{modulo}")
    public ResponseEntity<ModuloConfiguracaoResponse> atualizar(@AuthenticationPrincipal Usuario usuario,
                                                                 @PathVariable TipoModulo modulo,
                                                                 @Valid @RequestBody AtualizarModuloRequest request) {
        return ResponseEntity.ok(moduloConfiguracaoService.atualizar(usuario.getId(), modulo, request));
    }
}
