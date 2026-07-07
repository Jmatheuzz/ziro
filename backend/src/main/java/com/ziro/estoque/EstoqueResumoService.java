package com.ziro.estoque;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziro.common.EmpresaAccessService;
import com.ziro.estoque.dto.EstoqueResumoResponse;
import com.ziro.model.Empresa;
import com.ziro.model.ModuloConfiguracao;
import com.ziro.model.Produto;
import com.ziro.model.enums.TipoModulo;
import com.ziro.repository.ModuloConfiguracaoRepository;
import com.ziro.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueResumoService {

    private final ProdutoRepository produtoRepository;
    private final ModuloConfiguracaoRepository moduloConfiguracaoRepository;
    private final EmpresaAccessService empresaAccessService;
    private final ObjectMapper objectMapper;

    private record ConfigEstoque(boolean alertaEstoqueBaixo, int estoqueMinimoPadrao) {}

    private static final ConfigEstoque CONFIG_PADRAO = new ConfigEstoque(true, 5);

    @Transactional(readOnly = true)
    public EstoqueResumoResponse resumo(UUID usuarioId) {
        Empresa empresa = empresaAccessService.empresaComModuloAtivo(usuarioId, TipoModulo.ESTOQUE);
        ConfigEstoque config = configDoModulo(empresa.getId());

        List<Produto> produtos = produtoRepository.findByEmpresaIdAndAtivoTrue(empresa.getId());

        long totalProdutos = produtos.size();

        long produtosComEstoqueBaixo = produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() != null)
                .filter(p -> {
                    int limite = p.getEstoqueMinimo() != null ? p.getEstoqueMinimo() : config.estoqueMinimoPadrao();
                    return p.getQuantidadeEstoque() <= limite;
                })
                .count();

        BigDecimal valorTotalEstoque = produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() != null && p.getPrecoCusto() != null)
                .map(p -> p.getPrecoCusto().multiply(BigDecimal.valueOf(p.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EstoqueResumoResponse(
                totalProdutos,
                config.alertaEstoqueBaixo() ? produtosComEstoqueBaixo : 0,
                valorTotalEstoque
        );
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
}
