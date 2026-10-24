package com.KharchaPani.LenDenMicroservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class LenDenMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LenDenMicroserviceApplication.class, args);
	}

	@PostConstruct
	public void set() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}
}
