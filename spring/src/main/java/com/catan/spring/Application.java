package com.catan.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/**
 * Punt d'entrada del webservice de Catan.
 * Les entitats JPA són les del projecte Hibernate (paquet catan.models), que
 * queda fora de com.catan.spring; per això cal indicar-lo amb @EntityScan.
 */
@SpringBootApplication
@EntityScan("catan.models")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
