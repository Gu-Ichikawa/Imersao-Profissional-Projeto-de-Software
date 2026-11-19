package br.com.zentry.cardapio.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PratoRequest(

        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome,

        @Size(max = 500, message = "A descrição pode ter no máximo 500 caracteres.")
        String descricao,

        @NotNull(message = "Informe o preço de venda.")
        @PositiveOrZero(message = "O preço de venda não pode ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "O preço de venda aceita até 2 casas decimais.")
        BigDecimal precoVenda,

        @NotNull(message = "Informe a categoria do prato.")
        Long categoriaCardapioId
) {
}
