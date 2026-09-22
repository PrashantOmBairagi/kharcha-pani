package com.prashant.kharchapaniapplication;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class KharchaPaniApplication {

	public static void main(String[] args) {
		SpringApplication.run(KharchaPaniApplication.class, args);

	}
	@PostConstruct
	public void set() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}

}
