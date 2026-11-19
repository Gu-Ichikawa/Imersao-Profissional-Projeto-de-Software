package br.com.zentry.cardapio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VendaRequest(

        @NotNull(message = "Informe a quantidade.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        @Max(value = 999, message = "A quantidade pode ser no máximo 999.")
        Integer quantidade
) {
}
