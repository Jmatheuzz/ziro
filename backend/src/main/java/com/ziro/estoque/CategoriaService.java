package com.ziro.estoque;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.estoque.dto.CategoriaRequest;
import com.ziro.estoque.dto.CategoriaResponse;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.Categoria;
import com.ziro.model.Empresa;
import com.ziro.model.Usuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EmpresaAccessService empresaAccessService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(UUID usuarioId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        return categoriaRepository.findByEmpresaId(empresa.getId()).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public CategoriaResponse criar(UUID usuarioId, CategoriaRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        Empresa empresa = usuario.getEmpresa();

        Categoria categoria = new Categoria();
        categoria.setEmpresa(empresa);
        categoria.setNome(request.nome());
        categoriaRepository.save(categoria);

        auditoriaService.registrar(empresa, usuario, "CATEGORIA", categoria.getId(), "CRIACAO",
                "Categoria " + categoria.getNome() + " criada");

        return paraResponse(categoria);
    }

    @Transactional
    public void excluir(UUID usuarioId, UUID categoriaId) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        Empresa empresa = usuario.getEmpresa();

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada"));

        if (!categoria.getEmpresa().getId().equals(empresa.getId())) {
            throw new RecursoNaoEncontradoException("Categoria nao encontrada");
        }

        String nome = categoria.getNome();

        // produtos que usavam essa categoria ficam sem categoria (FK com ON DELETE SET NULL)
        categoriaRepository.delete(categoria);

        auditoriaService.registrar(empresa, usuario, "CATEGORIA", categoriaId, "EXCLUSAO",
                "Categoria " + nome + " excluida");
    }

    private CategoriaResponse paraResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
