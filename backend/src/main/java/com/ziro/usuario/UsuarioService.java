package com.ziro.usuario;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.auth.dto.MensagemResponse;
import com.ziro.exception.CredenciaisInvalidasException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.ModuloConfiguracao;
import com.ziro.model.Usuario;
import com.ziro.model.UsuarioModuloPermissao;
import com.ziro.model.enums.RoleUsuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.repository.ModuloConfiguracaoRepository;
import com.ziro.repository.UsuarioModuloPermissaoRepository;
import com.ziro.repository.UsuarioRepository;
import com.ziro.usuario.dto.AtualizarPerfilRequest;
import com.ziro.usuario.dto.TrocarSenhaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ModuloConfiguracaoRepository moduloConfiguracaoRepository;
    private final UsuarioModuloPermissaoRepository usuarioModuloPermissaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    /**
     * Modulos que esse usuario especifico pode ver: intersecao entre o que a
     * empresa tem ativo e (se for OPERADOR) o que foi liberado pra ele.
     * ADMIN sempre ve tudo que a empresa tem ativo.
     */
    @Transactional(readOnly = true)
    public List<TipoModulo> modulosVisiveis(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (usuario.getEmpresa() == null) {
            return List.of();
        }

        Set<TipoModulo> ativosNaEmpresa = moduloConfiguracaoRepository.findByEmpresaId(usuario.getEmpresa().getId()).stream()
                .filter(ModuloConfiguracao::isAtivo)
                .map(ModuloConfiguracao::getModulo)
                .collect(Collectors.toSet());

        if (usuario.getRole() == RoleUsuario.ADMIN) {
            return List.copyOf(ativosNaEmpresa);
        }

        Set<TipoModulo> permitidosAoOperador = usuarioModuloPermissaoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(UsuarioModuloPermissao::getModulo)
                .collect(Collectors.toSet());

        return ativosNaEmpresa.stream()
                .filter(permitidosAoOperador::contains)
                .toList();
    }

    @Transactional
    public void atualizarPerfil(UUID usuarioId, AtualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        usuario.setNome(request.nome());
        usuarioRepository.save(usuario);

        if (usuario.getEmpresa() != null) {
            auditoriaService.registrar(usuario.getEmpresa(), usuario, "USUARIO", usuario.getId(), "ATUALIZACAO",
                    "Perfil atualizado");
        }
    }

    @Transactional
    public MensagemResponse trocarSenha(UUID usuarioId, TrocarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);

        if (usuario.getEmpresa() != null) {
            auditoriaService.registrar(usuario.getEmpresa(), usuario, "USUARIO", usuario.getId(), "TROCA_SENHA",
                    "Senha alterada");
        }

        return new MensagemResponse("Senha alterada com sucesso.");
    }
}
