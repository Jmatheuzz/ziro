package com.ziro.estoque;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.estoque.dto.AjustarEstoqueRequest;
import com.ziro.estoque.dto.AtualizarProdutoRequest;
import com.ziro.estoque.dto.CriarProdutoRequest;
import com.ziro.estoque.dto.ProdutoResponse;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.Categoria;
import com.ziro.model.Empresa;
import com.ziro.model.ModuloConfiguracao;
import com.ziro.model.Produto;
import com.ziro.model.Usuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.repository.CategoriaRepository;
import com.ziro.repository.ModuloConfiguracaoRepository;
import com.ziro.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ModuloConfiguracaoRepository moduloConfiguracaoRepository;
    private final EmpresaAccessService empresaAccessService;
    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    /** Espelha a configuracaoJson do modulo ESTOQUE (ver ModuloConfiguracao). */
    private record ConfigEstoque(boolean alertaEstoqueBaixo, int estoqueMinimoPadrao) {}

    private static final ConfigEstoque CONFIG_PADRAO = new ConfigEstoque(true, 5);

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listar(UUID usuarioId, String busca, UUID categoriaId, Pageable pageable) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        ConfigEstoque config = configDoModulo(empresa.getId());

        Page<Produto> pagina;
        if (categoriaId != null) {
            pagina = produtoRepository.findByEmpresaIdAndAtivoTrueAndCategoriaId(empresa.getId(), categoriaId, pageable);
        } else if (busca != null && !busca.isBlank()) {
            pagina = produtoRepository.findByEmpresaIdAndAtivoTrueAndNomeContainingIgnoreCase(empresa.getId(), busca, pageable);
        } else {
            pagina = produtoRepository.findByEmpresaIdAndAtivoTrue(empresa.getId(), pageable);
        }

        return pagina.map(produto -> paraResponse(produto, config));
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(UUID usuarioId, UUID produtoId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        Produto produto = produtoDaEmpresa(empresa.getId(), produtoId);
        return paraResponse(produto, configDoModulo(empresa.getId()));
    }

    @Transactional
    public ProdutoResponse criar(UUID usuarioId, CriarProdutoRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        Empresa empresa = usuario.getEmpresa();

        Produto produto = new Produto();
        produto.setEmpresa(empresa);
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPrecoVenda(request.precoVenda());
        produto.setPrecoCusto(request.precoCusto());
        produto.setQuantidadeEstoque(request.quantidadeEstoque() != null ? request.quantidadeEstoque() : 0);
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setSku(request.sku());
        produto.setCategoria(categoriaDaEmpresaOuNull(empresa.getId(), request.categoriaId()));
        produto.setAtivo(true);

        produtoRepository.save(produto);

        auditoriaService.registrar(empresa, usuario, "PRODUTO", produto.getId(), "CRIACAO",
                "Produto " + produto.getNome() + " cadastrado");

        return paraResponse(produto, configDoModulo(empresa.getId()));
    }

    @Transactional
    public ProdutoResponse atualizar(UUID usuarioId, UUID produtoId, AtualizarProdutoRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        Empresa empresa = usuario.getEmpresa();
        Produto produto = produtoDaEmpresa(empresa.getId(), produtoId);

        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPrecoVenda(request.precoVenda());
        produto.setPrecoCusto(request.precoCusto());
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setSku(request.sku());
        produto.setCategoria(categoriaDaEmpresaOuNull(empresa.getId(), request.categoriaId()));

        produtoRepository.save(produto);

        auditoriaService.registrar(empresa, usuario, "PRODUTO", produto.getId(), "ATUALIZACAO",
                "Produto " + produto.getNome() + " atualizado");

        return paraResponse(produto, configDoModulo(empresa.getId()));
    }

    @Transactional
    public ProdutoResponse ajustarEstoque(UUID usuarioId, UUID produtoId, AjustarEstoqueRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        Empresa empresa = usuario.getEmpresa();
        Produto produto = produtoDaEmpresa(empresa.getId(), produtoId);

        int atual = produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0;
        int novaQuantidade = request.tipo() == TipoMovimentacao.ENTRADA
                ? atual + request.quantidade()
                : atual - request.quantidade();

        if (novaQuantidade < 0) {
            throw new OperacaoNaoPermitidaException(
                    "Essa saida deixaria o estoque negativo. Estoque atual: " + atual);
        }

        produto.setQuantidadeEstoque(novaQuantidade);
        produtoRepository.save(produto);

        String motivo = request.motivo() != null && !request.motivo().isBlank() ? " (" + request.motivo() + ")" : "";
        auditoriaService.registrar(empresa, usuario, "PRODUTO", produto.getId(), "AJUSTE_ESTOQUE",
                request.tipo().name() + " de " + request.quantidade() + " un. em " + produto.getNome()
                        + " - novo saldo: " + novaQuantidade + motivo);

        return paraResponse(produto, configDoModulo(empresa.getId()));
    }

    @Transactional
    public void excluir(UUID usuarioId, UUID produtoId) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        Empresa empresa = usuario.getEmpresa();
        Produto produto = produtoDaEmpresa(empresa.getId(), produtoId);

        // soft delete - preserva historico de vendas que ja referenciam esse produto
        produto.setAtivo(false);
        produtoRepository.save(produto);

        auditoriaService.registrar(empresa, usuario, "PRODUTO", produto.getId(), "EXCLUSAO",
                "Produto " + produto.getNome() + " excluido");
    }

    private Categoria categoriaDaEmpresaOuNull(UUID empresaId, UUID categoriaId) {
        if (categoriaId == null) return null;

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria nao encontrada"));

        if (!categoria.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Categoria nao encontrada");
        }
        return categoria;
    }

    private Produto produtoDaEmpresa(UUID empresaId, UUID produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado"));

        if (!produto.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Produto nao encontrado");
        }
        return produto;
    }

    private ConfigEstoque configDoModulo(UUID empresaId) {
        return moduloConfiguracaoRepository.findByEmpresaIdAndModulo(empresaId, TipoModulo.ESTOQUE)
                .map(ModuloConfiguracao::getConfiguracaoJson)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ConfigEstoque.class);
                    } catch (Exception e) {
                        return CONFIG_PADRAO;
                    }
                })
                .orElse(CONFIG_PADRAO);
    }

    private ProdutoResponse paraResponse(Produto produto, ConfigEstoque config) {
        int limiteMinimo = produto.getEstoqueMinimo() != null ? produto.getEstoqueMinimo() : config.estoqueMinimoPadrao();
        boolean estoqueBaixo = config.alertaEstoqueBaixo()
                && produto.getQuantidadeEstoque() != null
                && produto.getQuantidadeEstoque() <= limiteMinimo;

        Categoria categoria = produto.getCategoria();

        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPrecoVenda(),
                produto.getPrecoCusto(),
                produto.getQuantidadeEstoque(),
                produto.getEstoqueMinimo(),
                produto.getSku(),
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNome() : null,
                produto.isAtivo(),
                estoqueBaixo
        );
    }
}
