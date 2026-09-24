package com.mechanicmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Levanta la aplicación completa contra un MySQL real y descartable.
 *
 * <p>Sirve para detectar lo que los tests unitarios no ven: que todas las
 * migraciones de Flyway apliquen desde cero, que las entidades coincidan con
 * el esquema resultante ({@code ddl-auto=validate}) y que el contexto de Spring
 * arme todos sus beans. La base es de un solo uso, así que nunca toca los datos
 * de desarrollo.
 *
 * <p>Requiere Docker en ejecución.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class MechanicManagerApplicationTests {

	@Container
	@ServiceConnection
	static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

	@Test
	void contextLoads() {
	}

}
