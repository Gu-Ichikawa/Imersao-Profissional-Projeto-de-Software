package br.com.zentry.cardapio.dto;

import br.com.zentry.cardapio.model.Prato;
import java.math.BigDecimal;

public record PratoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal precoVenda,
        Boolean ativo,
        CategoriaCardapioResponse categoria
) {

    public static PratoResponse de(Prato prato) {
        return new PratoResponse(
                prato.getId(),
                prato.getNome(),
                prato.getDescricao(),
                prato.getPrecoVenda(),
                prato.getAtivo(),
                CategoriaCardapioResponse.de(prato.getCategoria()));
    }
}
