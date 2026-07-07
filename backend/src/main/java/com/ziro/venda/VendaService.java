package com.ziro.venda;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.common.EmpresaAccessService;
import com.ziro.exception.OperacaoNaoPermitidaException;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.Cliente;
import com.ziro.model.Empresa;
import com.ziro.model.Produto;
import com.ziro.model.Usuario;
import com.ziro.model.enums.FormaPagamento;
import com.ziro.model.enums.StatusConta;
import com.ziro.model.enums.StatusVenda;
import com.ziro.model.enums.TipoModulo;
import com.ziro.model.enums.TipoMovimentacao;
import com.ziro.model.financeiro.ContaReceber;
import com.ziro.model.financeiro.MovimentacaoCaixa;
import com.ziro.model.venda.ItemVenda;
import com.ziro.model.venda.Venda;
import com.ziro.repository.ClienteRepository;
import com.ziro.repository.ProdutoRepository;
import com.ziro.repository.financeiro.ContaReceberRepository;
import com.ziro.repository.financeiro.MovimentacaoCaixaRepository;
import com.ziro.repository.venda.VendaRepository;
import com.ziro.venda.dto.CriarVendaRequest;
import com.ziro.venda.dto.ItemVendaRequest;
import com.ziro.venda.dto.ItemVendaResponse;
import com.ziro.venda.dto.VendaResponse;
import com.ziro.venda.dto.VendasResumoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendaService {

    private static final long PRAZO_PADRAO_FIADO_DIAS = 30;

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final EmpresaAccessService empresaAccessService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<VendaResponse> listar(UUID usuarioId, StatusVenda status, Pageable pageable) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.VENDAS);

        Page<Venda> pagina = status == null
                ? vendaRepository.findByEmpresaIdOrderByDataVendaDescCriadoEmDesc(empresa.getId(), pageable)
                : vendaRepository.findByEmpresaIdAndStatusOrderByDataVendaDescCriadoEmDesc(empresa.getId(), status, pageable);

        return pagina.map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public VendaResponse buscarPorId(UUID usuarioId, UUID vendaId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.VENDAS);
        return paraResponse(vendaDaEmpresa(empresa.getId(), vendaId));
    }

    @Transactional
    public VendaResponse criar(UUID usuarioId, CriarVendaRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.VENDAS);
        Empresa empresa = usuario.getEmpresa();

        if (request.formaPagamento() == FormaPagamento.FIADO && request.clienteId() == null) {
            throw new OperacaoNaoPermitidaException("Venda fiado precisa ter um cliente vinculado");
        }

        Cliente cliente = clienteDaEmpresaOuNull(empresa.getId(), request.clienteId());

        Venda venda = new Venda();
        venda.setEmpresa(empresa);
        venda.setCliente(cliente);
        venda.setDataVenda(request.dataVenda() != null ? request.dataVenda() : LocalDate.now());
        venda.setFormaPagamento(request.formaPagamento());
        venda.setStatus(StatusVenda.ATIVA);
        venda.setObservacoes(request.observacoes());

        BigDecimal valorItens = BigDecimal.ZERO;
        List<ItemVenda> itens = new ArrayList<>();

        for (ItemVendaRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado"));

            if (!produto.getEmpresa().getId().equals(empresa.getId())) {
                throw new RecursoNaoEncontradoException("Produto nao encontrado");
            }
            if (!produto.isAtivo()) {
                throw new OperacaoNaoPermitidaException("Produto " + produto.getNome() + " nao esta mais disponivel");
            }
            if (produto.getQuantidadeEstoque() != null && produto.getQuantidadeEstoque() < itemRequest.quantidade()) {
                throw new OperacaoNaoPermitidaException(
                        "Estoque insuficiente pra " + produto.getNome() + ". Disponivel: " + produto.getQuantidadeEstoque());
            }

            if (produto.getQuantidadeEstoque() != null) {
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - itemRequest.quantidade());
                produtoRepository.save(produto);
            }

            BigDecimal subtotal = produto.getPrecoVenda().multiply(BigDecimal.valueOf(itemRequest.quantidade()));
            valorItens = valorItens.add(subtotal);

            ItemVenda item = new ItemVenda();
            item.setEmpresa(empresa);
            item.setVenda(venda);
            item.setProduto(produto);
            item.setNomeProduto(produto.getNome());
            item.setQuantidade(itemRequest.quantidade());
            item.setPrecoUnitario(produto.getPrecoVenda());
            item.setSubtotal(subtotal);
            itens.add(item);
        }

        BigDecimal desconto = request.desconto() != null ? request.desconto() : BigDecimal.ZERO;
        BigDecimal valorTotal = valorItens.subtract(desconto);

        if (valorTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new OperacaoNaoPermitidaException("O desconto nao pode ser maior que o valor da venda");
        }

        venda.setDesconto(desconto);
        venda.setValorTotal(valorTotal);
        venda.setItens(itens);

        vendaRepository.save(venda);

        registrarConsequenciaFinanceira(venda, empresa, cliente, valorTotal);
        vendaRepository.save(venda);

        auditoriaService.registrar(empresa, usuario, "VENDA", venda.getId(), "CRIACAO",
                "Venda de R$ " + valorTotal + " registrada" + (cliente != null ? " para " + cliente.getNome() : ""));

        return paraResponse(venda);
    }

    private void registrarConsequenciaFinanceira(Venda venda, Empresa empresa, Cliente cliente, BigDecimal valorTotal) {
        String codigoVenda = venda.getId().toString().substring(0, 8);

        if (venda.getFormaPagamento() == FormaPagamento.FIADO) {
            ContaReceber contaReceber = new ContaReceber();
            contaReceber.setEmpresa(empresa);
            contaReceber.setDescricao("Venda #" + codigoVenda);
            contaReceber.setValor(valorTotal);
            contaReceber.setDataVencimento(venda.getDataVenda().plusDays(PRAZO_PADRAO_FIADO_DIAS));
            contaReceber.setCliente(cliente);
            contaReceber.setStatus(StatusConta.ABERTA);
            contaReceberRepository.save(contaReceber);

            venda.setContaReceberId(contaReceber.getId());
        } else {
            MovimentacaoCaixa movimentacao = new MovimentacaoCaixa();
            movimentacao.setEmpresa(empresa);
            movimentacao.setTipo(TipoMovimentacao.ENTRADA);
            movimentacao.setValor(valorTotal);
            movimentacao.setDescricao("Venda #" + codigoVenda + " (" + venda.getFormaPagamento().name() + ")");
            movimentacao.setData(venda.getDataVenda());
            movimentacao.setOrigem("VENDA");
            movimentacaoCaixaRepository.save(movimentacao);

            venda.setMovimentacaoCaixaId(movimentacao.getId());
        }
    }

    @Transactional
    public void cancelar(UUID usuarioId, UUID vendaId) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.VENDAS);
        Empresa empresa = usuario.getEmpresa();
        Venda venda = vendaDaEmpresa(empresa.getId(), vendaId);

        if (venda.getStatus() != StatusVenda.ATIVA) {
            throw new OperacaoNaoPermitidaException("Essa venda ja esta cancelada");
        }

        // devolve o estoque de cada item
        for (ItemVenda item : venda.getItens()) {
            Produto produto = item.getProduto();
            if (produto != null && produto.getQuantidadeEstoque() != null) {
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        // reverte a consequencia financeira sem apagar o historico
        if (venda.getContaReceberId() != null) {
            contaReceberRepository.findById(venda.getContaReceberId()).ifPresent(conta -> {
                if (conta.getStatus() == StatusConta.ABERTA) {
                    conta.setStatus(StatusConta.CANCELADA);
                    contaReceberRepository.save(conta);
                }
            });
        }
        if (venda.getMovimentacaoCaixaId() != null) {
            MovimentacaoCaixa estorno = new MovimentacaoCaixa();
            estorno.setEmpresa(empresa);
            estorno.setTipo(TipoMovimentacao.SAIDA);
            estorno.setValor(venda.getValorTotal());
            estorno.setDescricao("Estorno da venda #" + venda.getId().toString().substring(0, 8) + " cancelada");
            estorno.setData(LocalDate.now());
            estorno.setOrigem("VENDA_CANCELAMENTO");
            movimentacaoCaixaRepository.save(estorno);
        }

        venda.setStatus(StatusVenda.CANCELADA);
        vendaRepository.save(venda);

        auditoriaService.registrar(empresa, usuario, "VENDA", venda.getId(), "CANCELAMENTO",
                "Venda cancelada - estoque e financeiro estornados");
    }

    @Transactional(readOnly = true)
    public VendasResumoResponse resumo(UUID usuarioId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.VENDAS);
        UUID empresaId = empresa.getId();

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fimMes = hoje.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal totalVendido = vendaRepository.somarValorPorPeriodo(empresaId, StatusVenda.ATIVA, inicioMes, fimMes);
        long quantidade = vendaRepository.countByEmpresaIdAndStatusAndDataVendaBetween(empresaId, StatusVenda.ATIVA, inicioMes, fimMes);

        BigDecimal ticketMedio = quantidade > 0
                ? totalVendido.divide(BigDecimal.valueOf(quantidade), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new VendasResumoResponse(totalVendido, quantidade, ticketMedio);
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

    private Venda vendaDaEmpresa(UUID empresaId, UUID vendaId) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda nao encontrada"));

        if (!venda.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Venda nao encontrada");
        }
        return venda;
    }

    private VendaResponse paraResponse(Venda venda) {
        Cliente cliente = venda.getCliente();

        List<ItemVendaResponse> itens = venda.getItens().stream()
                .map(item -> new ItemVendaResponse(
                        item.getProduto() != null ? item.getProduto().getId() : null,
                        item.getNomeProduto(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .toList();

        return new VendaResponse(
                venda.getId(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : null,
                venda.getDataVenda(),
                venda.getStatus(),
                venda.getFormaPagamento(),
                venda.getValorTotal(),
                venda.getDesconto(),
                venda.getObservacoes(),
                itens
        );
    }
}
