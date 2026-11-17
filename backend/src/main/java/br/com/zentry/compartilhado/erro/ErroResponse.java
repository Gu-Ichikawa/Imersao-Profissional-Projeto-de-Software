package br.com.zentry.compartilhado.erro;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(
		int status,
		String codigo,
		String mensagem,
		List<CampoErro> campos,
		LocalDateTime timestamp) {

	public static ErroResponse de(int status, String codigo, String mensagem) {
		return new ErroResponse(status, codigo, mensagem, List.of(), LocalDateTime.now().withNano(0));
	}

	public static ErroResponse deValidacao(List<CampoErro> campos) {
		return new ErroResponse(400, "VALIDACAO", "Existem campos inválidos.", campos,
				LocalDateTime.now().withNano(0));
	}
}
