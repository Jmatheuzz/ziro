package com.ziro.financeiro;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.financeiro.dto.AtualizarContaPagarRequest;
import com.ziro.financeiro.dto.ContaPagarResponse;
import com.ziro.financeiro.dto.CriarContaPagarRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContaPagarService {

    private final ContaPagarRepository contaPagarRepository;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final EmpresaAccessService empresaAccessService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<ContaPagarResponse> listar(UUID usuarioId, StatusConta status, Pageable pageable) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);

        Page<ContaPagar> pagina = status == null
                ? contaPagarRepository.findByEmpresaId(empresa.getId(), pageable)
                : contaPagarRepository.findByEmpresaIdAndStatus(empresa.getId(), status, pageable);

        return pagina.map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public ContaPagarResponse buscarPorId(UUID usuarioId, UUID contaId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        return paraResponse(contaDaEmpresa(empresa.getId(), contaId));
    }

    @Transactional
    public ContaPagarResponse criar(UUID usuarioId, CriarContaPagarRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();

        ContaPagar conta = new ContaPagar();
        conta.setEmpresa(empresa);
        conta.setDescricao(request.descricao());
        conta.setValor(request.valor());
        conta.setDataVencimento(request.dataVencimento());
        conta.setFornecedor(request.fornecedor());
        conta.setCategoria(request.categoria());
        conta.setStatus(StatusConta.ABERTA);

        contaPagarRepository.save(conta);

        auditoriaService.registrar(empresa, usuario, "CONTA_PAGAR", conta.getId(), "CRIACAO",
                "Conta a pagar criada: " + conta.getDescricao());

        return paraResponse(conta);
    }

    @Transactional
    public ContaPagarResponse atualizar(UUID usuarioId, UUID contaId, AtualizarContaPagarRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();
        ContaPagar conta = contaDaEmpresa(empresa.getId(), contaId);

        garantirAberta(conta.getStatus());

        conta.setDescricao(request.descricao());
        conta.setValor(request.valor());
        conta.setDataVencimento(request.dataVencimento());
        conta.setFornecedor(request.fornecedor());
        conta.setCategoria(request.categoria());

        contaPagarRepository.save(conta);

        auditoriaService.registrar(empresa, usuario, "CONTA_PAGAR", conta.getId(), "ATUALIZACAO",
                "Conta a pagar atualizada: " + conta.getDescricao());

        return paraResponse(conta);
    }

    @Transactional
    public ContaPagarResponse marcarComoPaga(UUID usuarioId, UUID contaId, MarcarPagaRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();
        ContaPagar conta = contaDaEmpresa(empresa.getId(), contaId);

        garantirAberta(conta.getStatus());

        LocalDate dataPagamento = request.dataPagamento() != null ? request.dataPagamento() : LocalDate.now();
        conta.setStatus(StatusConta.PAGA);
        conta.setDataPagamento(dataPagamento);
        contaPagarRepository.save(conta);

        MovimentacaoCaixa movimentacao = new MovimentacaoCaixa();
        movimentacao.setEmpresa(empresa);
        movimentacao.setTipo(TipoMovimentacao.SAIDA);
        movimentacao.setValor(conta.getValor());
        movimentacao.setDescricao("Pagamento: " + conta.getDescricao());
        movimentacao.setData(dataPagamento);
        movimentacao.setOrigem("CONTA_PAGAR");
        movimentacaoCaixaRepository.save(movimentacao);

        auditoriaService.registrar(empresa, usuario, "CONTA_PAGAR", conta.getId(), "PAGAMENTO",
                "Conta a pagar " + conta.getDescricao() + " marcada como paga");

        return paraResponse(conta);
    }

    @Transactional
    public void cancelar(UUID usuarioId, UUID contaId) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();
        ContaPagar conta = contaDaEmpresa(empresa.getId(), contaId);

        garantirAberta(conta.getStatus());

        conta.setStatus(StatusConta.CANCELADA);
        contaPagarRepository.save(conta);

        auditoriaService.registrar(empresa, usuario, "CONTA_PAGAR", conta.getId(), "CANCELAMENTO",
                "Conta a pagar " + conta.getDescricao() + " cancelada");
    }

    private void garantirAberta(StatusConta status) {
        if (status != StatusConta.ABERTA) {
            throw new OperacaoNaoPermitidaException("Essa conta ja foi paga ou cancelada e nao pode mais ser alterada");
        }
    }

    private ContaPagar contaDaEmpresa(UUID empresaId, UUID contaId) {
        ContaPagar conta = contaPagarRepository.findById(contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta a pagar nao encontrada"));

        if (!conta.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Conta a pagar nao encontrada");
        }
        return conta;
    }

    private ContaPagarResponse paraResponse(ContaPagar conta) {
        boolean atrasada = conta.getStatus() == StatusConta.ABERTA
                && conta.getDataVencimento().isBefore(LocalDate.now());

        return new ContaPagarResponse(
                conta.getId(),
                conta.getDescricao(),
                conta.getValor(),
                conta.getDataVencimento(),
                conta.getDataPagamento(),
                conta.getFornecedor(),
                conta.getCategoria(),
                conta.getStatus(),
                atrasada
        );
    }
}
