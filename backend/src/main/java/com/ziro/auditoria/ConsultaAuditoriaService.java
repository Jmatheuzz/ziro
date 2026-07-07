package com.ziro.auditoria;

import com.ziro.auditoria.dto.RegistroAuditoriaResponse;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.RegistroAuditoria;
import com.ziro.model.Usuario;
import com.ziro.model.enums.RoleUsuario;
import com.ziro.repository.RegistroAuditoriaRepository;
import com.ziro.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultaAuditoriaService {

    private final RegistroAuditoriaRepository registroAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<RegistroAuditoriaResponse> listar(UUID usuarioId, String entidade, Pageable pageable) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (usuario.getEmpresa() == null) {
            throw new RecursoNaoEncontradoException("Esse usuario ainda nao tem uma empresa cadastrada");
        }
        // historico de auditoria e informacao sensivel da empresa - so admin ve
        if (usuario.getRole() != RoleUsuario.ADMIN) {
            throw new OperacaoNaoPermitidaException("So o administrador da conta pode ver o historico de ações");
        }

        UUID empresaId = usuario.getEmpresa().getId();

        Page<RegistroAuditoria> pagina = (entidade == null || entidade.isBlank())
                ? registroAuditoriaRepository.findByEmpresaIdOrderByCriadoEmDesc(empresaId, pageable)
                : registroAuditoriaRepository.findByEmpresaIdAndEntidadeOrderByCriadoEmDesc(empresaId, entidade, pageable);

        return pagina.map(this::paraResponse);
    }

    private RegistroAuditoriaResponse paraResponse(RegistroAuditoria registro) {
        return new RegistroAuditoriaResponse(
                registro.getId(),
                registro.getUsuarioNome(),
                registro.getEntidade(),
                registro.getEntidadeId(),
                registro.getAcao(),
                registro.getDescricao(),
                registro.getCriadoEm()
        );
    }
}
