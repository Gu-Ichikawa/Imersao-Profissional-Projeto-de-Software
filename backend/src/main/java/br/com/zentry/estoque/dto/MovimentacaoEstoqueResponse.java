package br.com.zentry.estoque.dto;

import br.com.zentry.estoque.model.MotivoAjuste;
import br.com.zentry.estoque.model.MovimentacaoEstoque;
import br.com.zentry.estoque.model.TipoMovimentacaoEstoque;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoEstoqueResponse(
        Long id,
        TipoMovimentacaoEstoque tipo,
        BigDecimal quantidade,
        LocalDateTime dataHora,
        MotivoAjuste motivo
) {

    public static MovimentacaoEstoqueResponse de(MovimentacaoEstoque movimentacao) {
        return new MovimentacaoEstoqueResponse(
                movimentacao.getId(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getDataHora(),
                movimentacao.getAjuste() == null ? null : movimentacao.getAjuste().getMotivo());
    }
}
