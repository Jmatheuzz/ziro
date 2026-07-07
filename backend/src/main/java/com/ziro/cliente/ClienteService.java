package com.ziro.cliente;

import com.ziro.auditoria.AuditoriaService;
import com.ziro.cliente.dto.AtualizarClienteRequest;
import com.ziro.cliente.dto.ClienteResponse;
import com.ziro.cliente.dto.CriarClienteRequest;
import com.ziro.common.EmpresaAccessService;
import com.ziro.exception.RecursoNaoEncontradoException;
import com.ziro.model.Cliente;
import com.ziro.model.Empresa;
import com.ziro.model.Usuario;
import com.ziro.model.enums.TipoModulo;
import com.ziro.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaAccessService empresaAccessService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(UUID usuarioId, String busca, Pageable pageable) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.CLIENTES);

        Page<Cliente> pagina = (busca == null || busca.isBlank())
                ? clienteRepository.findByEmpresaIdAndAtivoTrue(empresa.getId(), pageable)
                : clienteRepository.findByEmpresaIdAndAtivoTrueAndNomeContainingIgnoreCase(empresa.getId(), busca, pageable);

        return pagina.map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(UUID usuarioId, UUID clienteId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.CLIENTES);
        Cliente cliente = clienteDaEmpresa(empresa.getId(), clienteId);
        return paraResponse(cliente);
    }

    @Transactional
    public ClienteResponse criar(UUID usuarioId, CriarClienteRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.CLIENTES);
        Empresa empresa = usuario.getEmpresa();

        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresa);
        cliente.setNome(request.nome());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setObservacoes(request.observacoes());
        cliente.setAtivo(true);

        clienteRepository.save(cliente);

        auditoriaService.registrar(empresa, usuario, "CLIENTE", cliente.getId(), "CRIACAO",
                "Cliente " + cliente.getNome() + " cadastrado");

        return paraResponse(cliente);
    }

    @Transactional
    public ClienteResponse atualizar(UUID usuarioId, UUID clienteId, AtualizarClienteRequest request) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.CLIENTES);
        Empresa empresa = usuario.getEmpresa();
        Cliente cliente = clienteDaEmpresa(empresa.getId(), clienteId);

        cliente.setNome(request.nome());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setObservacoes(request.observacoes());

        clienteRepository.save(cliente);

        auditoriaService.registrar(empresa, usuario, "CLIENTE", cliente.getId(), "ATUALIZACAO",
                "Cliente " + cliente.getNome() + " atualizado");

        return paraResponse(cliente);
    }

    @Transactional
    public void excluir(UUID usuarioId, UUID clienteId) {
        Usuario usuario = empresaAccessService.usuarioComModuloAtivo(usuarioId, TipoModulo.CLIENTES);
        Empresa empresa = usuario.getEmpresa();
        Cliente cliente = clienteDaEmpresa(empresa.getId(), clienteId);

        // soft delete - mantem o historico (vendas, contas a receber) intacto
        cliente.setAtivo(false);
        clienteRepository.save(cliente);

        auditoriaService.registrar(empresa, usuario, "CLIENTE", cliente.getId(), "EXCLUSAO",
                "Cliente " + cliente.getNome() + " excluido");
    }

    private Cliente clienteDaEmpresa(UUID empresaId, UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado"));

        if (!cliente.getEmpresa().getId().equals(empresaId)) {
            // nao revela que o cliente existe em outra empresa
            throw new RecursoNaoEncontradoException("Cliente nao encontrado");
        }
        return cliente;
    }

    private ClienteResponse paraResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getCpfCnpj(),
                cliente.getObservacoes(),
                cliente.isAtivo()
        );
    }
}
