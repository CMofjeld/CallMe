package com.callme.services.statusservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.callme")
public class StatusServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatusServiceApplication.class, args);
	}

}
