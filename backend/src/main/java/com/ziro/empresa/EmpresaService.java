package com.ziro.empresa;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.empresa.dto.AtualizarEmpresaRequest;
import com.ziro.empresa.dto.CriarEmpresaRequest;
import com.ziro.empresa.dto.EmpresaResponse;
import com.ziro.exception.EmpresaJaExisteException;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.Empresa;
import com.ziro.model.ModuloConfiguracao;
import com.ziro.model.Usuario;
import com.ziro.model.enums.RoleUsuario;
import com.ziro.model.enums.SegmentoNegocio;
import com.ziro.model.enums.TipoModulo;
import com.ziro.repository.EmpresaRepository;
import com.ziro.repository.ModuloConfiguracaoRepository;
import com.ziro.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModuloConfiguracaoRepository moduloConfiguracaoRepository;
    private final AuditoriaService auditoriaService;

    /**
     * Configuracao padrao de modulos por segmento - e aqui que mora a
     * "personalizacao sem complicacao": o dono nao precisa entender o sistema
     * pra comecar, a gente ja sugere o que faz sentido pro tipo de negocio dele.
     * Tudo continua editavel depois em Configuracoes > Personalizacao.
     */
    private static final Map<SegmentoNegocio, Set<TipoModulo>> MODULOS_SUGERIDOS_POR_SEGMENTO = new EnumMap<>(SegmentoNegocio.class);

    static {
        MODULOS_SUGERIDOS_POR_SEGMENTO.put(SegmentoNegocio.COMERCIO,
                Set.of(TipoModulo.FINANCEIRO, TipoModulo.ESTOQUE, TipoModulo.CLIENTES, TipoModulo.VENDAS));
        MODULOS_SUGERIDOS_POR_SEGMENTO.put(SegmentoNegocio.ALIMENTACAO,
                Set.of(TipoModulo.FINANCEIRO, TipoModulo.ESTOQUE, TipoModulo.CLIENTES, TipoModulo.VENDAS));
        MODULOS_SUGERIDOS_POR_SEGMENTO.put(SegmentoNegocio.SERVICOS,
                Set.of(TipoModulo.FINANCEIRO, TipoModulo.CLIENTES)); // servico geralmente nao tem estoque nem venda de produto
        MODULOS_SUGERIDOS_POR_SEGMENTO.put(SegmentoNegocio.OUTRO,
                Set.of(TipoModulo.FINANCEIRO, TipoModulo.CLIENTES));
    }

    @Transactional
    public EmpresaResponse criar(UUID usuarioId, CriarEmpresaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (usuario.getRole() != RoleUsuario.ADMIN) {
            throw new OperacaoNaoPermitidaException("So o administrador da conta pode criar a empresa");
        }
        if (usuario.getEmpresa() != null) {
            throw new EmpresaJaExisteException();
        }

        Empresa empresa = new Empresa();
        empresa.setNomeFantasia(request.nomeFantasia());
        empresa.setRazaoSocial(request.razaoSocial());
        empresa.setCnpjCpf(request.cnpjCpf());
        empresa.setSegmento(request.segmento());
        empresa.setAtiva(true);
        empresaRepository.save(empresa);

        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);

        criarModulosPadrao(empresa, request.segmento());

        auditoriaService.registrar(empresa, usuario, "EMPRESA", empresa.getId(), "CRIACAO",
                "Empresa " + empresa.getNomeFantasia() + " cadastrada");

        return paraResponse(empresa);
    }

    private void criarModulosPadrao(Empresa empresa, SegmentoNegocio segmento) {
        Set<TipoModulo> sugeridos = MODULOS_SUGERIDOS_POR_SEGMENTO.getOrDefault(segmento, Set.of());

        for (TipoModulo modulo : TipoModulo.values()) {
            ModuloConfiguracao config = new ModuloConfiguracao();
            config.setEmpresa(empresa);
            config.setModulo(modulo);
            config.setAtivo(sugeridos.contains(modulo));

            if (modulo == TipoModulo.ESTOQUE) {
                config.setConfiguracaoJson("{\"alertaEstoqueBaixo\": true, \"estoqueMinimoPadrao\": 5}");
            }

            moduloConfiguracaoRepository.save(config);
        }
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscarPorUsuario(UUID usuarioId) {
        Empresa empresa = empresaDoUsuario(usuarioId);
        return paraResponse(empresa);
    }

    @Transactional
    public EmpresaResponse atualizar(UUID usuarioId, AtualizarEmpresaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));
        Empresa empresa = empresaDoUsuario(usuarioId);

        empresa.setNomeFantasia(request.nomeFantasia());
        empresa.setRazaoSocial(request.razaoSocial());
        empresa.setCnpjCpf(request.cnpjCpf());
        empresa.setSegmento(request.segmento());
        empresaRepository.save(empresa);

        auditoriaService.registrar(empresa, usuario, "EMPRESA", empresa.getId(), "ATUALIZACAO",
                "Dados da empresa atualizados");

        return paraResponse(empresa);
    }

    private Empresa empresaDoUsuario(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));

        if (usuario.getEmpresa() == null) {
            throw new RecursoNaoEncontradoException("Esse usuario ainda nao tem uma empresa cadastrada");
        }
        return usuario.getEmpresa();
    }

    private EmpresaResponse paraResponse(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getNomeFantasia(),
                empresa.getRazaoSocial(),
                empresa.getCnpjCpf(),
                empresa.getSegmento(),
                empresa.isAtiva()
        );
    }
}
