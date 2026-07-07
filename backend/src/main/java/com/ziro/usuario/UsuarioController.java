package com.ziro.usuario;

import com.ziro.auth.dto.MensagemResponse;
import com.ziro.model.Usuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.usuario.dto.AtualizarPerfilRequest;
import com.ziro.usuario.dto.TrocarSenhaRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/me")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/modulos")
    public ResponseEntity<List<TipoModulo>> modulosVisiveis(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.modulosVisiveis(usuario.getId()));
    }

    @PutMapping
    public ResponseEntity<MensagemResponse> atualizarPerfil(@AuthenticationPrincipal Usuario usuario,
                                                             @Valid @RequestBody AtualizarPerfilRequest request) {
        usuarioService.atualizarPerfil(usuario.getId(), request);
        return ResponseEntity.ok(new MensagemResponse("Perfil atualizado com sucesso."));
    }

    @PostMapping("/senha")
    public ResponseEntity<MensagemResponse> trocarSenha(@AuthenticationPrincipal Usuario usuario,
                                                         @Valid @RequestBody TrocarSenhaRequest request) {
        return ResponseEntity.ok(usuarioService.trocarSenha(usuario.getId(), request));
    }
}
