package br.com.zentry.compartilhado.erro;

public class NaoEncontradoException extends RuntimeException {

	public NaoEncontradoException(String recurso, Object id) {
		super(recurso + " com id " + id + " não encontrado.");
	}
}
