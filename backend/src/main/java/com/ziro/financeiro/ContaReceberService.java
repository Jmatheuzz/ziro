package com.ziro.financeiro;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.financeiro.dto.AtualizarContaReceberRequest;
import com.ziro.financeiro.dto.ContaReceberResponse;
import com.ziro.financeiro.dto.CriarContaReceberRequest;
import com.ziro.financeiro.dto.MarcarRecebidaRequest;
import com.ziro.model.Cliente;
import com.ziro.model.Empresa;
import com.ziro.model.Usuario;
import com.ziro.model.enums.StatusConta;
import com.ziro.model.enums.TipoModulo;
import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.model.financeiro.ContaReceber;
import com.ziro.model.financeiro.MovimentacaoCaixa;
import com.ziro.repository.ClienteRepository;
import com.ziro.repository.financeiro.ContaReceberRepository;
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
public class ContaReceberService {

    private final ContaReceberRepository contaReceberRepository;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaAccessService empresaAccessService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<ContaReceberResponse> listar(UUID usuarioId, StatusConta status, Pageable pageable) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);

        Page<ContaReceber> pagina = status == null
                ? contaReceberRepository.findByEmpresaId(empresa.getId(), pageable)
                : contaReceberRepository.findByEmpresaIdAndStatus(empresa.getId(), status, pageable);

        return pagina.map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public ContaReceberResponse buscarPorId(UUID usuarioId, UUID contaId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        return paraResponse(contaDaEmpresa(empresa.getId(), contaId));
    }

    @Transactional
    public ContaReceberResponse criar(UUID usuarioId, CriarContaReceberRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();

        ContaReceber conta = new ContaReceber();
        conta.setEmpresa(empresa);
        conta.setDescricao(request.descricao());
        conta.setValor(request.valor());
        conta.setDataVencimento(request.dataVencimento());
        conta.setCliente(clienteDaEmpresaOuNull(empresa.getId(), request.clienteId()));
        conta.setStatus(StatusConta.ABERTA);

        contaReceberRepository.save(conta);

        auditoriaService.registrar(empresa, usuario, "CONTA_RECEBER", conta.getId(), "CRIACAO",
                "Conta a receber criada: " + conta.getDescricao());

        return paraResponse(conta);
    }

    @Transactional
    public ContaReceberResponse atualizar(UUID usuarioId, UUID contaId, AtualizarContaReceberRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();
        ContaReceber conta = contaDaEmpresa(empresa.getId(), contaId);

        garantirAberta(conta.getStatus());

        conta.setDescricao(request.descricao());
        conta.setValor(request.valor());
        conta.setDataVencimento(request.dataVencimento());
        conta.setCliente(clienteDaEmpresaOuNull(empresa.getId(), request.clienteId()));

        contaReceberRepository.save(conta);

        auditoriaService.registrar(empresa, usuario, "CONTA_RECEBER", conta.getId(), "ATUALIZACAO",
                "Conta a receber atualizada: " + conta.getDescricao());

        return paraResponse(conta);
    }

    @Transactional
    public ContaReceberResponse marcarComoRecebida(UUID usuarioId, UUID contaId, MarcarRecebidaRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();
        ContaReceber conta = contaDaEmpresa(empresa.getId(), contaId);

        garantirAberta(conta.getStatus());

        LocalDate dataRecebimento = request.dataRecebimento() != null ? request.dataRecebimento() : LocalDate.now();
        conta.setStatus(StatusConta.RECEBIDA);
        conta.setDataRecebimento(dataRecebimento);
        contaReceberRepository.save(conta);

        MovimentacaoCaixa movimentacao = new MovimentacaoCaixa();
        movimentacao.setEmpresa(empresa);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setValor(conta.getValor());
        movimentacao.setDescricao("Recebimento: " + conta.getDescricao());
        movimentacao.setData(dataRecebimento);
        movimentacao.setOrigem("CONTA_RECEBER");
        movimentacaoCaixaRepository.save(movimentacao);

        auditoriaService.registrar(empresa, usuario, "CONTA_RECEBER", conta.getId(), "RECEBIMENTO",
                "Conta a receber " + conta.getDescricao() + " marcada como recebida");

        return paraResponse(conta);
    }

    @Transactional
    public void cancelar(UUID usuarioId, UUID contaId) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.FINANCEIRO);
        Empresa empresa = usuario.getEmpresa();
        ContaReceber conta = contaDaEmpresa(empresa.getId(), contaId);

        garantirAberta(conta.getStatus());

        conta.setStatus(StatusConta.CANCELADA);
        contaReceberRepository.save(conta);

        auditoriaService.registrar(empresa, usuario, "CONTA_RECEBER", conta.getId(), "CANCELAMENTO",
                "Conta a receber " + conta.getDescricao() + " cancelada");
    }

    private void garantirAberta(StatusConta status) {
        if (status != StatusConta.ABERTA) {
            throw new OperacaoNaoPermitidaException("Essa conta ja foi recebida ou cancelada e nao pode mais ser alterada");
        }
    }

    private Cliente clienteDaEmpresaOuNull(UUID empresaId, UUID clienteId) {
        if (clienteId == null) return null;

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado"));

        if (!cliente.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Cliente nao encontrado");
        }
        return cliente;
    }

    private ContaReceber contaDaEmpresa(UUID empresaId, UUID contaId) {
        ContaReceber conta = contaReceberRepository.findById(contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta a receber nao encontrada"));

        if (!conta.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Conta a receber nao encontrada");
        }
        return conta;
    }

    private ContaReceberResponse paraResponse(ContaReceber conta) {
        boolean atrasada = conta.getStatus() == StatusConta.ABERTA
                && conta.getDataVencimento().isBefore(LocalDate.now());

        Cliente cliente = conta.getCliente();

        return new ContaReceberResponse(
                conta.getId(),
                conta.getDescricao(),
                conta.getValor(),
                conta.getDataVencimento(),
                conta.getDataRecebimento(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : null,
                conta.getStatus(),
                atrasada
        );
    }
}
