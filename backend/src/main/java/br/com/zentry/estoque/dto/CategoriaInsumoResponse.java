package br.com.zentry.estoque.dto;

import br.com.zentry.estoque.model.CategoriaInsumo;

public record CategoriaInsumoResponse(Long id, String nome) {

    public static CategoriaInsumoResponse de(CategoriaInsumo categoria) {
        return new CategoriaInsumoResponse(categoria.getId(), categoria.getNome());
    }
}
