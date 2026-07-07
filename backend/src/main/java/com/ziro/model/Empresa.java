package com.ziro.model;

import com.ziro.model.base.BaseEntity;
import com.ziro.model.enums.SegmentoNegocio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Empresa = o tenant do sistema. Cada microempreendedor/pequena empresa
 * que se cadastra no Ziro vira uma Empresa, e toda entidade de negocio
 * (produto, cliente, conta, etc) fica isolada por empresa_id.
 */
@Getter
@Setter
@Entity
@Table(name = "empresa")
public class Empresa extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String nomeFantasia;

    @Column(length = 150)
    private String razaoSocial;

    @Column(length = 20)
    private String cnpjCpf;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SegmentoNegocio segmento; // usado so pra sugerir modulos no onboarding

    @Column(nullable = false)
    private boolean ativa = true;
}
