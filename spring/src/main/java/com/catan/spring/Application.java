package com.catan.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

// L'anotació @EntityScan indica a Spring Boot on buscar les entitats JPA. En aquest cas, les entitats es troben al paquet "catan.models"
@SpringBootApplication
@EntityScan("catan.models")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
