package com.ziro.auditoria;

import com.ziro.model.Empresa;
import com.ziro.model.RegistroAuditoria;
import com.ziro.model.Usuario;
import com.ziro.repository.RegistroAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Ponto unico onde qualquer modulo registra uma acao na trilha de auditoria.
 * Chamado a partir dos services de negocio (cliente, financeiro, estoque, etc)
 * logo apos uma mudanca de estado ser persistida com sucesso.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final RegistroAuditoriaRepository registroAuditoriaRepository;

    @Transactional
    public void registrar(Empresa empresa, Usuario usuario, String entidade, UUID entidadeId,
                           String acao, String descricao) {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setEmpresa(empresa);
        registro.setUsuario(usuario);
        registro.setUsuarioNome(usuario != null ? usuario.getNome() : null);
        registro.setEntidade(entidade);
        registro.setEntidadeId(entidadeId);
        registro.setAcao(acao);
        registro.setDescricao(descricao);

        registroAuditoriaRepository.save(registro);
    }
}
