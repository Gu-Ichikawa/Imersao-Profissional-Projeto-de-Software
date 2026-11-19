package br.com.zentry.cardapio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaCardapioRequest(

        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome
) {
}
