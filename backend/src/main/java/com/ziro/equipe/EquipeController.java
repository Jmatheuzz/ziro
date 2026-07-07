package com.ziro.equipe;

import com.ziro.equipe.dto.AtualizarPermissoesRequest;
import com.ziro.equipe.dto.ConvidarOperadorRequest;
import com.ziro.equipe.dto.OperadorResponse;
import com.ziro.model.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipe")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;

    @GetMapping
    public ResponseEntity<List<OperadorResponse>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(equipeService.listar(usuario.getId()));
    }

    @PostMapping("/convites")
    public ResponseEntity<OperadorResponse> convidar(@AuthenticationPrincipal Usuario usuario,
                                                      @Valid @RequestBody ConvidarOperadorRequest request) {
        OperadorResponse resposta = equipeService.convidar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("/{operadorId}/permissoes")
    public ResponseEntity<OperadorResponse> atualizarPermissoes(@AuthenticationPrincipal Usuario usuario,
                                                                 @PathVariable UUID operadorId,
                                                                 @Valid @RequestBody AtualizarPermissoesRequest request) {
        return ResponseEntity.ok(equipeService.atualizarPermissoes(usuario.getId(), operadorId, request));
    }

    @PatchMapping("/{operadorId}/desativar")
    public ResponseEntity<Void> desativar(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID operadorId) {
        equipeService.desativar(usuario.getId(), operadorId);
        return ResponseEntity.noContent().build();
    }
}
