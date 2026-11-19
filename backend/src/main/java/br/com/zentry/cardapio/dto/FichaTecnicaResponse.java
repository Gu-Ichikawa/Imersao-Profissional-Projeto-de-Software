package br.com.zentry.cardapio.dto;

import br.com.zentry.cardapio.model.FichaTecnica;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FichaTecnicaResponse(
        Long pratoId,
        String pratoNome,
        String categoriaNome,
        BigDecimal precoVenda,
        BigDecimal custoTotal,
        LocalDateTime atualizadaEm,
        List<ItemFichaResponse> itens
) {

    public static FichaTecnicaResponse de(FichaTecnica ficha) {
        return new FichaTecnicaResponse(
                ficha.getPrato().getId(),
                ficha.getPrato().getNome(),
                ficha.getPrato().getCategoria().getNome(),
                ficha.getPrato().getPrecoVenda(),
                ficha.getCustoTotal(),
                ficha.getAtualizadaEm(),
                ficha.getItens().stream().map(ItemFichaResponse::de).toList());
    }
}
