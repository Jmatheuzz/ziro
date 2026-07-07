package com.ziro.equipe;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.email.EmailService;
import com.ziro.equipe.dto.AtualizarPermissoesRequest;
import com.ziro.equipe.dto.ConvidarOperadorRequest;
import com.ziro.equipe.dto.OperadorResponse;
import com.ziro.exception.EmailJaCadastradoException;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.Empresa;
import com.ziro.model.Usuario;
import com.ziro.model.UsuarioModuloPermissao;
import com.ziro.model.auth.TokenRecuperacaoSenha;
import com.ziro.model.enums.RoleUsuario;
import com.ziro.model.enums.StatusUsuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.repository.TokenRecuperacaoSenhaRepository;
import com.ziro.repository.UsuarioModuloPermissaoRepository;
import com.ziro.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipeService {

    private static final long EXPIRACAO_TOKEN_CONVITE_HORAS = 72;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioModuloPermissaoRepository usuarioModuloPermissaoRepository;
    private final TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<OperadorResponse> listar(UUID usuarioId) {
        Usuario admin = adminValidado(usuarioId);

        return usuarioRepository.findByEmpresaIdAndRole(admin.getEmpresa().getId(), RoleUsuario.OPERADOR).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public OperadorResponse convidar(UUID usuarioId, ConvidarOperadorRequest request) {
        Usuario admin = adminValidado(usuarioId);
        Empresa empresa = admin.getEmpresa();

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }

        Usuario operador = new Usuario();
        operador.setNome(request.nome());
        operador.setEmail(request.email());
        // senha placeholder impossivel de bater - o convidado so consegue entrar depois de definir a propria senha
        operador.setSenhaHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        operador.setRole(RoleUsuario.OPERADOR);
        operador.setStatus(StatusUsuario.ATIVO);
        operador.setEmpresa(empresa);
        usuarioRepository.save(operador);

        concederPermissoes(operador, request.modulos());

        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setCodigo(UUID.randomUUID().toString());
        token.setUsuario(operador);
        token.setExpiraEm(LocalDateTime.now().plusHours(EXPIRACAO_TOKEN_CONVITE_HORAS));
        tokenRecuperacaoSenhaRepository.save(token);

        emailService.enviarEmailConviteEquipe(operador.getEmail(), operador.getNome(), empresa.getNomeFantasia(), token.getCodigo());

        auditoriaService.registrar(empresa, admin, "USUARIO", operador.getId(), "CRIACAO",
                "Operador " + operador.getNome() + " convidado pra equipe");

        return paraResponse(operador);
    }

    @Transactional
    public OperadorResponse atualizarPermissoes(UUID usuarioId, UUID operadorId, AtualizarPermissoesRequest request) {
        Usuario admin = adminValidado(usuarioId);
        Usuario operador = operadorDaEmpresa(admin.getEmpresa().getId(), operadorId);

        concederPermissoes(operador, request.modulos() != null ? request.modulos() : Set.of());

        auditoriaService.registrar(admin.getEmpresa(), admin, "USUARIO", operador.getId(), "ATUALIZACAO",
                "Permissoes de " + operador.getNome() + " atualizadas");

        return paraResponse(operador);
    }

    @Transactional
    public void desativar(UUID usuarioId, UUID operadorId) {
        Usuario admin = adminValidado(usuarioId);
        Usuario operador = operadorDaEmpresa(admin.getEmpresa().getId(), operadorId);

        operador.setStatus(StatusUsuario.INATIVO);
        usuarioRepository.save(operador);

        auditoriaService.registrar(admin.getEmpresa(), admin, "USUARIO", operador.getId(), "DESATIVACAO",
                "Acesso de " + operador.getNome() + " revogado");
    }

    private void concederPermissoes(Usuario operador, Set<TipoModulo> modulos) {
        usuarioModuloPermissaoRepository.deleteByUsuarioId(operador.getId());

        for (TipoModulo modulo : modulos) {
            UsuarioModuloPermissao permissao = new UsuarioModuloPermissao();
            permissao.setUsuario(operador);
            permissao.setModulo(modulo);
            usuarioModuloPermissaoRepository.save(permissao);
        }
    }

    private Usuario adminValidado(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (usuario.getEmpresa() == null) {
            throw new RecursoNaoEncontradoException("Esse usuario ainda nao tem uma empresa cadastrada");
        }
        if (usuario.getRole() != RoleUsuario.ADMIN) {
            throw new OperacaoNaoPermitidaException("So o administrador da conta pode gerenciar a equipe");
        }
        return usuario;
    }

    private Usuario operadorDaEmpresa(UUID empresaId, UUID operadorId) {
        Usuario operador = usuarioRepository.findById(operadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Operador nao encontrado"));

        if (operador.getEmpresa() == null || !operador.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Operador nao encontrado");
        }
        if (operador.getRole() != RoleUsuario.OPERADOR) {
            throw new OperacaoNaoPermitidaException("Esse usuario nao e um operador");
        }
        return operador;
    }

    private OperadorResponse paraResponse(Usuario operador) {
        Set<TipoModulo> modulos = usuarioModuloPermissaoRepository.findByUsuarioId(operador.getId()).stream()
                .map(UsuarioModuloPermissao::getModulo)
                .collect(Collectors.toSet());

        return new OperadorResponse(operador.getId(), operador.getNome(), operador.getEmail(), operador.getStatus(), modulos);
    }
}
