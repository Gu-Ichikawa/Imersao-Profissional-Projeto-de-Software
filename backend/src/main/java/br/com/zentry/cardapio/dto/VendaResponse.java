package br.com.zentry.cardapio.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado da venda: quanto saiu de cada insumo e com quanto ele ficou.
 * Insumos sem controle de estoque não aparecem em "baixas" (RN-CAR-006).
 */
public record VendaResponse(
        Long pratoId,
        String prato,
        Integer quantidade,
        List<BaixaInsumo> baixas
) {

    public record BaixaInsumo(
            Long insumoId,
            String insumo,
            String unidadeMedida,
            BigDecimal quantidadeBaixada,
            BigDecimal saldoAtual
    ) {
    }
}
