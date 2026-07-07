package com.ziro.model.base;

import com.ziro.model.Empresa;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Toda entidade que pertence a uma empresa (tenant) herda daqui.
 * Estrategia de multi-tenancy: coluna empresa_id em cada tabela (row-level),
 * mais simples de operar e escalar num primeiro momento do que schema-per-tenant.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class TenantAwareEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}
