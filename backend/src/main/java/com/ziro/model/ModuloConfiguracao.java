package com.ziro.model;

import com.ziro.model.base.TenantAwareEntity;
import com.ziro.model.enums.TipoModulo;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

/**
 * Define quais modulos estao ativos pra uma empresa (ex: FINANCEIRO, ESTOQUE)
 * e guarda preferencias especificas daquele modulo num JSONB, sem precisar
 * criar coluna nova no banco toda vez que surge uma preferencia nova.
 * Ex de configuracaoJson pro modulo ESTOQUE: {"alertaEstoqueBaixo": true, "estoqueMinimoPadrao": 5}
 */
@Getter
@Setter
@Entity
@Table(name = "modulo_configuracao",
        uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "modulo"}))
public class ModuloConfiguracao extends TenantAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoModulo modulo;

    @Column(nullable = false)
    private boolean ativo = true;

    @Type(JsonType.class)
    @Column(name = "configuracao_json", columnDefinition = "jsonb")
    private String configuracaoJson;
}
