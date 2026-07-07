package com.ziro.empresa.dto;

/**
 * Ambos os campos sao opcionais - manda so o que quer atualizar.
 * ativo == null significa "nao mexe no estado atual".
 * configuracaoJson == null significa "nao mexe na configuracao atual".
 */
public record AtualizarModuloRequest(
        Boolean ativo,
        String configuracaoJson
) {}
