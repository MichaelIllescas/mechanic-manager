package com.mechanicmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
public class MechanicManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MechanicManagerApplication.class, args);
	}

	@Bean
	Clock appClock(@Value("${app.time-zone:America/Argentina/Buenos_Aires}") String timeZone) {
		return Clock.system(ZoneId.of(timeZone));
	}

}
