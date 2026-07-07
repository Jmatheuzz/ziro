package com.ziro.cliente;

import com.ziro.cliente.dto.AtualizarClienteRequest;
import com.ziro.cliente.dto.ClienteResponse;
import com.ziro.cliente.dto.CriarClienteRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<Page<ClienteResponse>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(clienteService.listar(usuario.getId(), busca, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscar(@AuthenticationPrincipal Usuario usuario,
                                                   @PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscarPorId(usuario.getId(), id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@AuthenticationPrincipal Usuario usuario,
                                                  @Valid @RequestBody CriarClienteRequest request) {
        ClienteResponse resposta = clienteService.criar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@AuthenticationPrincipal Usuario usuario,
                                                      @PathVariable UUID id,
                                                      @Valid @RequestBody AtualizarClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Usuario usuario, @PathVariable UUID id) {
        clienteService.excluir(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
