package com.mechanicmanager;

import org.junit.jupiter.api.Test;

/**
 * Levanta la aplicación completa contra un MySQL real y descartable.
 *
 * <p>Sirve para detectar lo que los tests unitarios no ven: que todas las
 * migraciones de Flyway apliquen desde cero, que las entidades coincidan con
 * el esquema resultante ({@code ddl-auto=validate}) y que el contexto de Spring
 * arme todos sus beans. La base es de un solo uso, así que nunca toca los datos
 * de desarrollo.
 */
class MechanicManagerApplicationTests extends IntegrationTestBase {

	@Test
	void contextLoads() {
	}

}
