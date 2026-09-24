package com.mechanicmanager;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Base de los tests que levantan la aplicación completa contra un MySQL real y
 * descartable.
 *
 * <p>Todas las subclases comparten la misma configuración, así que Spring
 * reutiliza un único contexto (y un único contenedor) en toda la corrida en
 * lugar de arrancar uno por clase. Requiere Docker en ejecución.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

	@Container
	@ServiceConnection
	static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

}
