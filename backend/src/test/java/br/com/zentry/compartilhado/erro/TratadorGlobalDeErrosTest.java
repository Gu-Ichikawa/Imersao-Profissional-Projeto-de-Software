package br.com.zentry.compartilhado.erro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Testa só o tratador de erros, sem subir a aplicação nem o banco
class TratadorGlobalDeErrosTest {

	@RestController
	static class ControllerDeTeste {

		@GetMapping("/api/teste/nao-encontrado")
		String naoEncontrado() {
			throw new NaoEncontradoException("Insumo", 5);
		}
	}

	private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
			.setControllerAdvice(new TratadorGlobalDeErros())
			.build();

	@Test
	void rotaInexistenteDevolve404() throws Exception {
		mockMvc.perform(get("/api/rota-que-nao-existe"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("ROTA_NAO_ENCONTRADA"));
	}

	@Test
	void recursoInexistenteContinuaDevolvendo404() throws Exception {
		mockMvc.perform(get("/api/teste/nao-encontrado"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"))
				.andExpect(jsonPath("$.mensagem").value("Insumo com id 5 não encontrado."));
	}
}
