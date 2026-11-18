package br.com.zentry.estoque.dto;

import br.com.zentry.estoque.model.UnidadeMedida;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record InsumoRequest(

        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome,

        @Size(max = 50, message = "O SKU pode ter no máximo 50 caracteres.")
        String sku,

        @NotNull(message = "Informe a unidade de medida.")
        UnidadeMedida unidadeMedida,

        @NotNull(message = "Informe se o insumo é perecível.")
        Boolean perecivel,

        @NotNull(message = "Informe o estoque mínimo.")
        @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
        @Digits(integer = 9, fraction = 3, message = "O estoque mínimo aceita até 3 casas decimais.")
        BigDecimal estoqueMinimo,

        @NotNull(message = "Informe o custo unitário.")
        @PositiveOrZero(message = "O custo unitário não pode ser negativo.")
        @Digits(integer = 8, fraction = 4, message = "O custo unitário aceita até 4 casas decimais.")
        BigDecimal custoUnitario,

        @NotNull(message = "Informe se o insumo controla estoque.")
        Boolean controlaEstoque,

        @NotNull(message = "Informe a categoria do insumo.")
        Long categoriaInsumoId,

        @PositiveOrZero(message = "A quantidade inicial não pode ser negativa.")
        @Digits(integer = 9, fraction = 3, message = "A quantidade inicial aceita até 3 casas decimais.")
        BigDecimal quantidadeInicial
) {
}
