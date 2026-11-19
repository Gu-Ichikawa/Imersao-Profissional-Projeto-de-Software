package br.com.zentry.estoque.dto;

import br.com.zentry.estoque.model.MotivoAjuste;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record AjusteEstoqueRequest(

        @NotNull(message = "Informe o insumo.")
        Long insumoId,

        @NotNull(message = "Informe a quantidade contada.")
        @PositiveOrZero(message = "A quantidade contada não pode ser negativa.")
        @Digits(integer = 9, fraction = 3, message = "A quantidade contada aceita até 3 casas decimais.")
        BigDecimal quantidadeContada,

        @NotNull(message = "Informe o motivo do ajuste.")
        MotivoAjuste motivo
) {
}
