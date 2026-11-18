package br.com.zentry.estoque.dto;

import br.com.zentry.estoque.model.Insumo;
import br.com.zentry.estoque.model.NivelEstoque;
import br.com.zentry.estoque.model.UnidadeMedida;
import java.math.BigDecimal;

public record InsumoResponse(
        Long id,
        String nome,
        String sku,
        UnidadeMedida unidadeMedida,
        Boolean perecivel,
        BigDecimal estoqueMinimo,
        BigDecimal custoUnitario,
        Boolean controlaEstoque,
        Boolean ativo,
        BigDecimal quantidadeAtual,
        NivelEstoque nivelEstoque,
        CategoriaInsumoResponse categoria
) {

    public static InsumoResponse de(Insumo insumo) {
        return new InsumoResponse(
                insumo.getId(),
                insumo.getNome(),
                insumo.getSku(),
                insumo.getUnidadeMedida(),
                insumo.getPerecivel(),
                insumo.getEstoqueMinimo(),
                insumo.getCustoUnitario(),
                insumo.getControlaEstoque(),
                insumo.getAtivo(),
                insumo.getQuantidadeAtual(),
                insumo.nivelEstoque(),
                CategoriaInsumoResponse.de(insumo.getCategoria()));
    }
}
