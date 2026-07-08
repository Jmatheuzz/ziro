package com.ziro.financeiro;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.financeiro.dto.MarcarPagaRequest;
import com.ziro.model.Empresa;
import com.ziro.model.Usuario;
import com.ziro.model.enums.StatusConta;
import com.ziro.model.enums.TipoModulo;
import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.model.financeiro.ContaPagar;
import com.ziro.model.financeiro.MovimentacaoCaixa;
import com.ziro.repository.financeiro.ContaPagarRepository;
import com.ziro.repository.financeiro.MovimentacaoCaixaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaPagarServiceTest {

    @Mock
    private ContaPagarRepository contaPagarRepository;
    @Mock
    private MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    @Mock
    private EmpresaAccessService empresaAccessService;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ContaPagarService service;

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

    private ContaPagar contaAberta(Empresa empresa, BigDecimal valor) {
        ContaPagar conta = new ContaPagar();
        conta.setId(UUID.randomUUID());
        conta.setEmpresa(empresa);
        conta.setDescricao("Conta Teste");
        conta.setValor(valor);
        conta.setDataVencimento(LocalDate.now().plusDays(7));
        conta.setStatus(StatusConta.ABERTA);
        return conta;
    }

    // ── testes ───────────────────────────────────────────────────────────────

    @Test
    void marcarComoPaga_contaAberta_mudaStatusParaPaga() {
        Usuario usuario = usuarioComEmpresa();
        ContaPagar conta = contaAberta(usuario.getEmpresa(), new BigDecimal("200.00"));

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.FINANCEIRO))
                .thenReturn(usuario);
        when(contaPagarRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(contaPagarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimentacaoCaixaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.marcarComoPaga(usuario.getId(), conta.getId(), new MarcarPagaRequest(null));

        ArgumentCaptor<ContaPagar> captor = ArgumentCaptor.forClass(ContaPagar.class);
        verify(contaPagarRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusConta.PAGA);
    }

    @Test
    void marcarComoPaga_contaAberta_criaMovimentacaoCaixaSaida() {
        Usuario usuario = usuarioComEmpresa();
        ContaPagar conta = contaAberta(usuario.getEmpresa(), new BigDecimal("200.00"));

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.FINANCEIRO))
                .thenReturn(usuario);
        when(contaPagarRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(contaPagarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimentacaoCaixaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.marcarComoPaga(usuario.getId(), conta.getId(), new MarcarPagaRequest(LocalDate.now()));

        ArgumentCaptor<MovimentacaoCaixa> captor = ArgumentCaptor.forClass(MovimentacaoCaixa.class);
        verify(movimentacaoCaixaRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
        assertThat(captor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void marcarComoPaga_contaJaPaga_lancaExcecao() {
        Usuario usuario = usuarioComEmpresa();
        ContaPagar conta = contaAberta(usuario.getEmpresa(), new BigDecimal("100.00"));
        conta.setStatus(StatusConta.PAGA);

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.FINANCEIRO))
                .thenReturn(usuario);
        when(contaPagarRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

        assertThatThrownBy(() ->
                service.marcarComoPaga(usuario.getId(), conta.getId(), new MarcarPagaRequest(null)))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("paga ou cancelada");
    }

    @Test
    void cancelar_contaAberta_mudaStatusParaCancelada() {
        Usuario usuario = usuarioComEmpresa();
        ContaPagar conta = contaAberta(usuario.getEmpresa(), new BigDecimal("150.00"));

        when(empresaAccessService.usuarioComModuloAtivo(usuario.getId(), TipoModulo.FINANCEIRO))
                .thenReturn(usuario);
        when(contaPagarRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(contaPagarRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.cancelar(usuario.getId(), conta.getId());

        ArgumentCaptor<ContaPagar> captor = ArgumentCaptor.forClass(ContaPagar.class);
        verify(contaPagarRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusConta.CANCELADA);
    }
}
