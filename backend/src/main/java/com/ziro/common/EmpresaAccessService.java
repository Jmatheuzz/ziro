package com.ziro.common;

import com.ziro.exception.ModuloInativoException;
import com.ziro.exception.PermissaoModuloNegadaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.Empresa;
import com.ziro.model.Usuario;
import com.ziro.model.enums.RoleUsuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.repository.ModuloConfiguracaoRepository;
import com.ziro.repository.UsuarioModuloPermissaoRepository;
import com.ziro.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Ponto unico onde qualquer modulo de negocio (clientes, financeiro, estoque...)
 * resolve "qual e a empresa desse usuario, o modulo X ta ligado pra ela, e esse
 * usuario especifico tem permissao pra usar esse modulo?".
 * Evita duplicar essa checagem em cada service novo.
 */
@Component
@RequiredArgsConstructor
public class EmpresaAccessService {

    private final UsuarioRepository usuarioRepository;
    private final ModuloConfiguracaoRepository moduloConfiguracaoRepository;
    private final UsuarioModuloPermissaoRepository usuarioModuloPermissaoRepository;

    public Empresa empresaComModuloAtivo(UUID usuarioId, TipoModulo modulo) {
        return usuarioComModuloAtivo(usuarioId, modulo).getEmpresa();
    }

    /**
     * Igual a empresaComModuloAtivo, mas devolve o Usuario tambem -
     * usado pelos services que precisam registrar quem fez a acao na auditoria.
     *
     * Regra de acesso: o modulo precisa estar ativo pra empresa E, se o usuario
     * for OPERADOR (nao ADMIN), ele precisa ter permissao explicita pra esse modulo.
     */
    public Usuario usuarioComModuloAtivo(UUID usuarioId, TipoModulo modulo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (usuario.getEmpresa() == null) {
            throw new RecursoNaoEncontradoException("Esse usuario ainda nao tem uma empresa cadastrada");
        }

        Empresa empresa = usuario.getEmpresa();
        boolean ativoNaEmpresa = moduloConfiguracaoRepository
                .existsByEmpresaIdAndModuloAndAtivoTrue(empresa.getId(), modulo);

        if (!ativoNaEmpresa) {
            throw new ModuloInativoException(modulo);
        }

        if (usuario.getRole() == RoleUsuario.OPERADOR) {
            boolean temPermissao = usuarioModuloPermissaoRepository
                    .existsByUsuarioIdAndModulo(usuario.getId(), modulo);
            if (!temPermissao) {
                throw new PermissaoModuloNegadaException(modulo);
            }
        }

        return usuario;
    }
}
