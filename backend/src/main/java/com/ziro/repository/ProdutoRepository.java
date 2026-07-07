package com.ziro.repository;

import com.ziro.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    Page<Produto> findByEmpresaIdAndAtivoTrue(UUID empresaId, Pageable pageable);

    Page<Produto> findByEmpresaIdAndAtivoTrueAndNomeContainingIgnoreCase(UUID empresaId, String nome, Pageable pageable);

    Page<Produto> findByEmpresaIdAndAtivoTrueAndCategoriaId(UUID empresaId, UUID categoriaId, Pageable pageable);

    // usado no resumo do estoque - volume esperado e pequeno o suficiente pra calcular em memoria
    List<Produto> findByEmpresaIdAndAtivoTrue(UUID empresaId);
}
