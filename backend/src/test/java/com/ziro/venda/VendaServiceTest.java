package com.ziro.venda;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.model.Cliente;
import com.ziro.model.Empresa;
import com.ziro.model.Produto;
import com.ziro.model.Usuario;
import com.ziro.model.enums.FormaPagamento;
import com.ziro.model.enums.TipoModulo;
import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.model.financeiro.ContaReceber;
import com.ziro.model.financeiro.MovimentacaoCaixa;
import com.ziro.model.venda.Venda;
import com.ziro.repository.ClienteRepository;
import com.ziro.repository.ProdutoRepository;
import com.ziro.repository.financeiro.ContaReceberRepository;
import com.ziro.repository.financeiro.MovimentacaoCaixaRepository;
import com.ziro.repository.venda.VendaRepository;
import com.ziro.venda.dto.CriarVendaRequest;
import com.ziro.venda.dto.ItemVendaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ContaReceberRepository contaReceberRepository;
    @Mock
    private MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    @Mock
    private EmpresaAccessService empresaAccessService;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private VendaService service;

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

    private Produto produtoAtivo(Empresa empresa, int estoque) {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setEmpresa(empresa);
        produto.setNome("Produto Teste");
        produto.setPrecoVenda(new BigDecimal("50.00"));
        produto.setQuantidadeEstoque(estoque);
        produto.setAtivo(true);
        return produto;
    }

    // vendaRepository.save() e chamado duas vezes: a primeira gera o ID, a segunda atualiza referencias financeiras
    private void mockVendaRepository() {
        when(vendaRepository.save(any())).thenAnswer(i -> {
            Venda v = i.getArgument(0);
            if (v.getId() == null) v.setId(UUID.randomUUID());
            return v;
        });
    }

    // ── testes ───────────────────────────────────────────────────────────────

    @Test
    void criar_fiadoSemCliente_lancaExcecaoSemConsultarEstoque() {
        Usuario usuario = usuarioComEmpresa();
        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.VENDAS))
                .thenReturn(usuario);

        var request = new CriarVendaRequest(null, null, FormaPagamento.FIADO, null, null,
                List.of(new ItemVendaRequest(UUID.randomUUID(), 1)));

        assertThatThrownBy(() -> service.criar(usuario.getId(), request))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("cliente");

        verify(produtoRepository, never()).findById(any());
    }

    @Test
    void criar_estoqueInsuficiente_lancaExcecao() {
        Usuario usuario = usuarioComEmpresa();
        Produto produto = produtoAtivo(usuario.getEmpresa(), 2);

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.VENDAS))
                .thenReturn(usuario);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));

        var request = new CriarVendaRequest(null, null, FormaPagamento.DINHEIRO, null, null,
                List.of(new ItemVendaRequest(produto.getId(), 5)));

        assertThatThrownBy(() -> service.criar(usuario.getId(), request))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void criar_estoqueSuficiente_descontaEstoqueDosProdutos() {
        Usuario usuario = usuarioComEmpresa();
        Produto produto = produtoAtivo(usuario.getEmpresa(), 10);

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.VENDAS))
                .thenReturn(usuario);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        mockVendaRepository();
        when(movimentacaoCaixaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.criar(usuario.getId(), new CriarVendaRequest(null, null, FormaPagamento.DINHEIRO,
                null, null, List.of(new ItemVendaRequest(produto.getId(), 3))));

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantidadeEstoque()).isEqualTo(7); // 10 - 3
    }

    @Test
    void criar_pagamentoAVista_criaMovimentacaoCaixaEntrada() {
        Usuario usuario = usuarioComEmpresa();
        Produto produto = produtoAtivo(usuario.getEmpresa(), 10);

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.VENDAS))
                .thenReturn(usuario);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        mockVendaRepository();
        when(movimentacaoCaixaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.criar(usuario.getId(), new CriarVendaRequest(null, null, FormaPagamento.PIX,
                null, null, List.of(new ItemVendaRequest(produto.getId(), 2))));

        ArgumentCaptor<MovimentacaoCaixa> captor = ArgumentCaptor.forClass(MovimentacaoCaixa.class);
        verify(movimentacaoCaixaRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
        assertThat(captor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("100.00")); // 50 x 2
    }

    @Test
    void criar_pagamentoFiado_criaContaReceberEnaoCriaMovimentacao() {
        Usuario usuario = usuarioComEmpresa();
        Empresa empresa = usuario.getEmpresa();
        Produto produto = produtoAtivo(empresa, 10);

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setEmpresa(empresa);
        cliente.setNome("Cliente Teste");

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.VENDAS))
                .thenReturn(usuario);
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        mockVendaRepository();
        when(contaReceberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.criar(usuario.getId(), new CriarVendaRequest(cliente.getId(), null, FormaPagamento.FIADO,
                null, null, List.of(new ItemVendaRequest(produto.getId(), 1))));

        ArgumentCaptor<ContaReceber> captor = ArgumentCaptor.forClass(ContaReceber.class);
        verify(contaReceberRepository).save(captor.capture());
        assertThat(captor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("50.00"));
        verify(movimentacaoCaixaRepository, never()).save(any());
    }
}
