package com.ziro.model;

import com.ziro.model.base.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Trilha de auditoria da empresa: quem fez o que, quando. Existe desde ja
 * pensando em quando o sistema tiver funcionarios/prestadores com roles -
 * a empresa vai precisar conseguir ver quem mexeu em cada coisa.
 * Nunca e editado ou apagado depois de criado.
 */
@Getter
@Setter
@Entity
@Table(name = "registro_auditoria")
public class RegistroAuditoria extends TenantAwareEntity {

    /** Quem fez a acao. Pode ser nulo se um dia existir uma acao automatica do sistema. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Nome do usuario no momento da acao - guardado aqui pra sobreviver mesmo se o usuario for removido depois. */
    @Column(name = "usuario_nome", length = 150)
    private String usuarioNome;

    /** Tipo de entidade afetada: CLIENTE, PRODUTO, CONTA_PAGAR, CONTA_RECEBER, MODULO, EMPRESA, USUARIO, etc. */
    @Column(nullable = false, length = 50)
    private String entidade;

    /** Id do registro afetado, quando aplicavel. */
    @Column(name = "entidade_id")
    private java.util.UUID entidadeId;

    /** Acao em si: CRIACAO, ATUALIZACAO, EXCLUSAO, ou uma acao especifica como PAGAMENTO, AJUSTE_ESTOQUE. */
    @Column(nullable = false, length = 50)
    private String acao;

    /** Descricao pronta pra exibir na tela, ja em portugues e com o contexto (ex: "Cliente Maria Silva criado"). */
    @Column(nullable = false, length = 300)
    private String descricao;
}
