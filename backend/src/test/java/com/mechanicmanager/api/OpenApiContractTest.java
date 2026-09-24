package com.mechanicmanager.api;

import com.mechanicmanager.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que {@code docs/04-api/openapi.yaml} describa la API tal como está
 * hoy (ADR-008: el contrato se actualiza en el mismo cambio que los endpoints).
 *
 * <p>El contrato se genera desde el propio código, así que no se edita a mano.
 * Si este test falla porque cambió un endpoint, un DTO o una anotación de
 * documentación, regenerarlo con:
 *
 * <pre>
 * ./mvnw test -Dtest=OpenApiContractTest -Dopenapi.update=true
 * </pre>
 *
 * y commitear el archivo resultante junto con el cambio.
 */
class OpenApiContractTest extends IntegrationTestBase {

	/** Surefire corre con el directorio del módulo (backend/) como directorio de trabajo. */
	private static final Path CONTRACT = Path.of("..", "docs", "04-api", "openapi.yaml");

	@Autowired
	private MockMvc mockMvc;

	/**
	 * La ruta .yaml no está en la lista pública de SecurityConfig (sólo lo está
	 * /v3/api-docs), así que el pedido va autenticado en vez de abrir otra ruta.
	 */
	@Test
	void theCommittedContractMatchesTheApi() throws Exception {
		String generated = normalize(mockMvc.perform(get("/v3/api-docs.yaml").with(user("contract").roles("USER")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8));

		if (Boolean.getBoolean("openapi.update")) {
			Files.writeString(CONTRACT, generated, StandardCharsets.UTF_8);
			return;
		}

		assertThat(normalize(Files.readString(CONTRACT, StandardCharsets.UTF_8)))
				.as("docs/04-api/openapi.yaml quedó desactualizado. Regeneralo con: "
						+ "./mvnw test -Dtest=OpenApiContractTest -Dopenapi.update=true")
				.isEqualTo(generated);
	}

	private static String normalize(String yaml) {
		return yaml.replace("\r\n", "\n").stripTrailing() + "\n";
	}

}
