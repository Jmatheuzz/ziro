package com.ziro.model;

import com.ziro.model.base.TenantAwareEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@Table(name = "cliente")
public class Cliente extends TenantAwareEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String cpfCnpj;

    @Column(length = 500)
    private String observacoes;

    @Type(JsonType.class)
    @Column(name = "atributos_json", columnDefinition = "jsonb")
    private String atributosJson;

    @Column(nullable = false)
    private boolean ativo = true;
}
