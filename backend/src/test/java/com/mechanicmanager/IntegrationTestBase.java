package com.mechanicmanager;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Base de los tests que levantan la aplicación completa contra un MySQL real y
 * descartable.
 *
 * <p>Todas las subclases comparten la misma configuración, así que Spring
 * reutiliza un único contexto en toda la corrida. El contenedor se arranca una
 * sola vez (singleton) y no con {@code @Container}: esa anotación lo detiene al
 * terminar cada clase y el contexto cacheado quedaría apuntando a una base
 * muerta. Testcontainers lo elimina al cerrar la JVM. Requiere Docker.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

	@ServiceConnection
	static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

	static {
		MYSQL.start();
	}

}
