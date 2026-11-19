package br.com.zentry.cardapio.dto;

import br.com.zentry.cardapio.model.CategoriaCardapio;

public record CategoriaCardapioResponse(Long id, String nome, Boolean ativa) {

    public static CategoriaCardapioResponse de(CategoriaCardapio categoria) {
        return new CategoriaCardapioResponse(categoria.getId(), categoria.getNome(), categoria.getAtiva());
    }
}
