package com.ziro.empresa;

import com.ziro.empresa.dto.AtualizarEmpresaRequest;
import com.ziro.empresa.dto.CriarEmpresaRequest;
import com.ziro.empresa.dto.EmpresaResponse;
import com.ziro.model.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<EmpresaResponse> criar(@AuthenticationPrincipal Usuario usuario,
                                                  @Valid @RequestBody CriarEmpresaRequest request) {
        EmpresaResponse resposta = empresaService.criar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/me")
    public ResponseEntity<EmpresaResponse> buscarMinhaEmpresa(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(empresaService.buscarPorUsuario(usuario.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<EmpresaResponse> atualizarMinhaEmpresa(@AuthenticationPrincipal Usuario usuario,
                                                                  @Valid @RequestBody AtualizarEmpresaRequest request) {
        return ResponseEntity.ok(empresaService.atualizar(usuario.getId(), request));
    }
}
