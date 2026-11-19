package br.com.zentry.cardapio.dto;

import br.com.zentry.cardapio.model.ItemFichaTecnica;
import br.com.zentry.estoque.model.UnidadeMedida;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record ItemFichaResponse(
        Long id,
        Long insumoId,
        String insumoNome,
        UnidadeMedida unidadeMedida,
        BigDecimal custoUnitario,
        BigDecimal quantidade,
        BigDecimal custo
) {

    public static ItemFichaResponse de(ItemFichaTecnica item) {
        return new ItemFichaResponse(
                item.getId(),
                item.getInsumo().getId(),
                item.getInsumo().getNome(),
                item.getInsumo().getUnidadeMedida(),
                item.getInsumo().getCustoUnitario(),
                item.getQuantidade(),
                item.custo().setScale(4, RoundingMode.HALF_UP));
    }
}
