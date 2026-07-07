package com.ziro.model;

import com.ziro.model.base.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categoria")
public class Categoria extends TenantAwareEntity {

    @Column(nullable = false, length = 100)
    private String nome;
}
