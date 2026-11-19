package br.com.zentry.cardapio.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ItemFichaRequest(

        @NotNull(message = "Informe o insumo.")
        Long insumoId,

        @NotNull(message = "Informe a quantidade.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        @Digits(integer = 9, fraction = 3, message = "A quantidade aceita até 3 casas decimais.")
        BigDecimal quantidade
) {
}
