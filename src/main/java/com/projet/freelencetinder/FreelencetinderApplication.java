 package com.projet.freelencetinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class FreelencetinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreelencetinderApplication.class, args);
	}

}
