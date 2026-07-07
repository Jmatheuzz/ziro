package com.ziro.model;

import com.ziro.model.base.BaseEntity;
import com.ziro.model.enums.TipoModulo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Concede a um usuario OPERADOR acesso a um modulo especifico.
 * ADMIN nunca precisa disso - ele sempre enxerga tudo que a empresa tem ativo.
 * Um OPERADOR so acessa um modulo se: (1) a empresa tem o modulo ativo E
 * (2) existe uma linha aqui ligando esse usuario a esse modulo.
 */
@Getter
@Setter
@Entity
@Table(name = "usuario_modulo_permissao",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "modulo"}))
public class UsuarioModuloPermissao extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoModulo modulo;
}
