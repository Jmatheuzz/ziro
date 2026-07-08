package com.ziro.estoque;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.estoque.dto.AjustarEstoqueRequest;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.model.Empresa;
import com.ziro.model.Produto;
import com.ziro.model.Usuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.repository.CategoriaRepository;
import com.ziro.repository.ModuloConfiguracaoRepository;
import com.ziro.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ModuloConfiguracaoRepository moduloConfiguracaoRepository;
    @Mock
    private EmpresaAccessService empresaAccessService;
    @Mock
    private AuditoriaService auditoriaService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProdutoService service;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Usuario usuarioComEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setNomeFantasia("Empresa Teste");

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmpresa(empresa);
        return usuario;
    }

    private Produto produtoComEstoque(Empresa empresa, int quantidade) {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setEmpresa(empresa);
        produto.setNome("Produto Teste");
        produto.setPrecoVenda(BigDecimal.TEN);
        produto.setQuantidadeEstoque(quantidade);
        produto.setAtivo(true);
        return produto;
    }

    // ── testes ───────────────────────────────────────────────────────────────

    @Test
    void ajustarEstoque_entrada_aumentaQuantidade() {
        Usuario usuario = usuarioComEmpresa();
        Produto produto = produtoComEstoque(usuario.getEmpresa(), 10);

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.ESTOQUE))
                .thenReturn(usuario);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        when(moduloConfiguracaoRepository.findByEmpresaIdAndModulo(any(), any()))
                .thenReturn(Optional.empty());
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.ajustarEstoque(usuario.getId(), produto.getId(),
                new AjustarEstoqueRequest(TipoMovimentacao.ENTRADA, 5, null));

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(15);
    }

    @Test
    void ajustarEstoque_saida_reduzQuantidade() {
        Usuario usuario = usuarioComEmpresa();
        Produto produto = produtoComEstoque(usuario.getEmpresa(), 10);

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.ESTOQUE))
                .thenReturn(usuario);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        when(moduloConfiguracaoRepository.findByEmpresaIdAndModulo(any(), any()))
                .thenReturn(Optional.empty());
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.ajustarEstoque(usuario.getId(), produto.getId(),
                new AjustarEstoqueRequest(TipoMovimentacao.SAIDA, 3, null));

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(7);
    }

    @Test
    void ajustarEstoque_saidaDeixaEstoqueNegativo_lancaExcecao() {
        Usuario usuario = usuarioComEmpresa();
        Produto produto = produtoComEstoque(usuario.getEmpresa(), 2);

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.ESTOQUE))
                .thenReturn(usuario);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.ajustarEstoque(usuario.getId(), produto.getId(),
                new AjustarEstoqueRequest(TipoMovimentacao.SAIDA, 5, null)))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("negativo");
    }
}
