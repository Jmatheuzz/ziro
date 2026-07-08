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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaAccessServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ModuloConfiguracaoRepository moduloConfiguracaoRepository;
    @Mock
    private UsuarioModuloPermissaoRepository usuarioModuloPermissaoRepository;

    @InjectMocks
    private EmpresaAccessService service;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Empresa empresaAtiva() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setNomeFantasia("Empresa Teste");
        return empresa;
    }

    private Usuario usuarioAdmin(Empresa empresa) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setRole(RoleUsuario.ADMIN);
        usuario.setEmpresa(empresa);
        return usuario;
    }

    private Usuario usuarioOperador(Empresa empresa) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setRole(RoleUsuario.OPERADOR);
        usuario.setEmpresa(empresa);
        return usuario;
    }

    // ── testes ───────────────────────────────────────────────────────────────

    @Test
    void usuarioComModuloAtivo_usuarioNaoEncontrado_lancaExcecao() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.usuarioComModuloAtivo(usuarioId, TipoModulo.ESTOQUE))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void usuarioComModuloAtivo_semEmpresa_lancaExcecao() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEmpresa(null);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.usuarioComModuloAtivo(usuarioId, TipoModulo.ESTOQUE))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void usuarioComModuloAtivo_moduloInativo_lancaExcecao() {
        Empresa empresa = empresaAtiva();
        Usuario usuario = usuarioAdmin(empresa);
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(moduloConfiguracaoRepository
                .existsByEmpresaIdAndModuloAndAtivoTrue(empresa.getId(), TipoModulo.ESTOQUE))
                .thenReturn(false);

        assertThatThrownBy(() -> service.usuarioComModuloAtivo(usuario.getId(), TipoModulo.ESTOQUE))
                .isInstanceOf(ModuloInativoException.class);
    }

    @Test
    void usuarioComModuloAtivo_adminNaoVerificaPermissaoIndividual() {
        Empresa empresa = empresaAtiva();
        Usuario usuario = usuarioAdmin(empresa);
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(moduloConfiguracaoRepository
                .existsByEmpresaIdAndModuloAndAtivoTrue(empresa.getId(), TipoModulo.ESTOQUE))
                .thenReturn(true);

        Usuario resultado = service.usuarioComModuloAtivo(usuario.getId(), TipoModulo.ESTOQUE);

        assertThat(resultado).isSameAs(usuario);
        verify(usuarioModuloPermissaoRepository, never()).existsByUsuarioIdAndModulo(any(), any());
    }

    @Test
    void usuarioComModuloAtivo_operadorSemPermissao_lancaExcecao() {
        Empresa empresa = empresaAtiva();
        Usuario usuario = usuarioOperador(empresa);
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(moduloConfiguracaoRepository
                .existsByEmpresaIdAndModuloAndAtivoTrue(empresa.getId(), TipoModulo.ESTOQUE))
                .thenReturn(true);
        when(usuarioModuloPermissaoRepository
                .existsByUsuarioIdAndModulo(usuario.getId(), TipoModulo.ESTOQUE))
                .thenReturn(false);

        assertThatThrownBy(() -> service.usuarioComModuloAtivo(usuario.getId(), TipoModulo.ESTOQUE))
                .isInstanceOf(PermissaoModuloNegadaException.class);
    }

    @Test
    void usuarioComModuloAtivo_operadorComPermissao_retornaUsuario() {
        Empresa empresa = empresaAtiva();
        Usuario usuario = usuarioOperador(empresa);
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(moduloConfiguracaoRepository
                .existsByEmpresaIdAndModuloAndAtivoTrue(empresa.getId(), TipoModulo.ESTOQUE))
                .thenReturn(true);
        when(usuarioModuloPermissaoRepository
                .existsByUsuarioIdAndModulo(usuario.getId(), TipoModulo.ESTOQUE))
                .thenReturn(true);

        Usuario resultado = service.usuarioComModuloAtivo(usuario.getId(), TipoModulo.ESTOQUE);

        assertThat(resultado).isSameAs(usuario);
    }
}
