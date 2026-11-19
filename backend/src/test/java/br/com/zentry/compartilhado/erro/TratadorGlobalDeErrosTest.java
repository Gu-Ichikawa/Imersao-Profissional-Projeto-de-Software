package br.com.zentry.compartilhado.erro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class TratadorGlobalDeErrosTest {

	@Autowired
	private WebApplicationContext contexto;

	private MockMvc mockMvc;

	@BeforeEach
	void configurar() {
		mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
	}

	@Test
	void rotaInexistenteDevolve404() throws Exception {
		mockMvc.perform(get("/api/rota-que-nao-existe"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("ROTA_NAO_ENCONTRADA"));
	}

	@Test
	void recursoInexistenteContinuaDevolvendo404() throws Exception {
		mockMvc.perform(get("/api/insumos/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
	}
}
