package br.com.zentry.cardapio.dto;

import br.com.zentry.cardapio.model.CategoriaCardapio;

public record CategoriaCardapioResponse(Long id, String nome) {

    public static CategoriaCardapioResponse de(CategoriaCardapio categoria) {
        return new CategoriaCardapioResponse(categoria.getId(), categoria.getNome());
    }
}
