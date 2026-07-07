package com.ziro.model.enums;

/**
 * Papel do usuario dentro da empresa (tenant).
 * ADMIN: dono/gestor da empresa, acesso total aos modulos habilitados.
 * OPERADOR: colaborador com acesso operacional (vendas, estoque, clientes).
 */
public enum RoleUsuario {
    ADMIN,
    OPERADOR
}
