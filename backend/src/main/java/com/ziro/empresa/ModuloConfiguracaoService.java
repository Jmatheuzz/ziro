package com.ziro.empresa;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.empresa.dto.AtualizarModuloRequest;
import com.ziro.empresa.dto.ModuloConfiguracaoResponse;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.Empresa;
import com.ziro.model.ModuloConfiguracao;
import com.ziro.model.Usuario;
import com.ziro.model.enums.RoleUsuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.repository.ModuloConfiguracaoRepository;
import com.ziro.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModuloConfiguracaoService {

    private final ModuloConfiguracaoRepository moduloConfiguracaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<ModuloConfiguracaoResponse> listar(UUID usuarioId) {
        Empresa empresa = empresaDoUsuario(usuarioId);

        return moduloConfiguracaoRepository.findByEmpresaId(empresa.getId()).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public ModuloConfiguracaoResponse atualizar(UUID usuarioId, TipoModulo modulo, AtualizarModuloRequest request) {
        Usuario usuario = usuarioComEmpresaValidado(usuarioId);
        Empresa empresa = usuario.getEmpresa();

        ModuloConfiguracao config = moduloConfiguracaoRepository.findByEmpresaIdAndModulo(empresa.getId(), modulo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Modulo nao encontrado pra essa empresa"));

        boolean ativoAnterior = config.isAtivo();

        if (request.ativo() != null) {
            config.setAtivo(request.ativo());
        }
        if (request.configuracaoJson() != null) {
            config.setConfiguracaoJson(request.configuracaoJson());
        }

        moduloConfiguracaoRepository.save(config);

        if (request.ativo() != null && request.ativo() != ativoAnterior) {
            auditoriaService.registrar(empresa, usuario, "MODULO", config.getId(), "ATUALIZACAO",
                    "Modulo " + modulo.name() + (config.isAtivo() ? " ativado" : " desativado"));
        } else if (request.configuracaoJson() != null) {
            auditoriaService.registrar(empresa, usuario, "MODULO", config.getId(), "ATUALIZACAO",
                    "Configuracao do modulo " + modulo.name() + " atualizada");
        }

        return paraResponse(config);
    }

    private Empresa empresaDoUsuario(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (usuario.getEmpresa() == null) {
            throw new RecursoNaoEncontradoException("Esse usuario ainda nao tem uma empresa cadastrada");
        }
        return usuario.getEmpresa();
    }

    private Usuario usuarioComEmpresaValidado(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (usuario.getEmpresa() == null) {
            throw new RecursoNaoEncontradoException("Esse usuario ainda nao tem uma empresa cadastrada");
        }
        if (usuario.getRole() != RoleUsuario.ADMIN) {
            throw new OperacaoNaoPermitidaException("So o administrador da conta pode alterar os modulos");
        }
        return usuario;
    }

    private ModuloConfiguracaoResponse paraResponse(ModuloConfiguracao config) {
        return new ModuloConfiguracaoResponse(
                config.getId(),
                config.getModulo(),
                config.isAtivo(),
                config.getConfiguracaoJson()
        );
    }
}
