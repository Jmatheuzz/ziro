package com.ziro.model;

import com.ziro.model.base.TenantAwareEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "produto")
public class Produto extends TenantAwareEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(precision = 12, scale = 2)
    private BigDecimal precoCusto;

    /** Controle de estoque so faz sentido se o modulo ESTOQUE estiver ativo pra empresa. */
    private Integer quantidadeEstoque;

    private Integer estoqueMinimo;

    @Column(length = 50)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Atributos especificos do negocio que nao fazem sentido virar coluna fixa
     * (ex: {"tamanho": "M", "cor": "azul"} pra loja de roupa,
     * {"validade": "2026-08-01"} pra alimenticio). Isso e o que da a sensacao
     * de "personalizado pro meu negocio" sem precisar de schema dinamico complexo.
     */
    @Type(JsonType.class)
    @Column(name = "atributos_json", columnDefinition = "jsonb")
    private String atributosJson;
}
