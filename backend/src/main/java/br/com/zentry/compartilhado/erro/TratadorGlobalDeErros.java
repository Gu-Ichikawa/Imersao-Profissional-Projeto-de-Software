package br.com.zentry.compartilhado.erro;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class TratadorGlobalDeErros {

	@ExceptionHandler(NaoEncontradoException.class)
	public ResponseEntity<ErroResponse> naoEncontrado(NaoEncontradoException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ErroResponse.de(404, "NAO_ENCONTRADO", ex.getMessage()));
	}

	@ExceptionHandler(RegraNegocioException.class)
	public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
		int status = ex.getStatus().value();
		return ResponseEntity.status(status)
				.body(ErroResponse.de(status, ex.getCodigo(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
		List<CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
				.map(erro -> new CampoErro(erro.getField(), erro.getDefaultMessage()))
				.toList();
		return ResponseEntity.badRequest().body(ErroResponse.deValidacao(campos));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest()
				.body(ErroResponse.de(400, "CORPO_INVALIDO", "O corpo da requisição está inválido ou mal formatado."));
	}

	@ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
	public ResponseEntity<ErroResponse> rotaNaoEncontrada(HttpServletRequest requisicao) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ErroResponse.de(404, "ROTA_NAO_ENCONTRADA",
						"A rota " + requisicao.getMethod() + " " + requisicao.getRequestURI() + " não existe."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> inesperado(Exception ex) {
		log.error("Erro inesperado", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErroResponse.de(500, "ERRO_INTERNO", "Erro inesperado. Tente novamente."));
	}
}
