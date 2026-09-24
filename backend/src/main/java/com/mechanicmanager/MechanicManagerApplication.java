package com.mechanicmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
public class MechanicManagerApplication {

	static final String DEFAULT_TIME_ZONE = "America/Argentina/Buenos_Aires";

	public static void main(String[] args) {
		// Antes de arrancar Spring: LocalDate.now(), las validaciones @FutureOrPresent
		// y la conexion a la base toman la zona por defecto de la JVM. En un servidor
		// suele ser UTC, y entre las 21:00 y las 24:00 de Argentina "hoy" ya seria manana.
		TimeZone.setDefault(TimeZone.getTimeZone(configuredTimeZone()));
		SpringApplication.run(MechanicManagerApplication.class, args);
	}

	static String configuredTimeZone() {
		String configured = System.getenv("APP_TIME_ZONE");
		return configured == null || configured.isBlank() ? DEFAULT_TIME_ZONE : configured.trim();
	}

	@Bean
	Clock appClock(@Value("${app.time-zone:" + DEFAULT_TIME_ZONE + "}") String timeZone) {
		return Clock.system(ZoneId.of(timeZone));
	}

}
