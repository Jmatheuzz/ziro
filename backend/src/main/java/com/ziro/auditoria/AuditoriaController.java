package com.ziro.auditoria;

import com.ziro.auditoria.dto.RegistroAuditoriaResponse;
import com.ziro.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final ConsultaAuditoriaService consultaAuditoriaService;

    @GetMapping
    public ResponseEntity<Page<RegistroAuditoriaResponse>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) String entidade,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(consultaAuditoriaService.listar(usuario.getId(), entidade, pageable));
    }
}
