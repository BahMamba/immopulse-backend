package com.mamba.immopulse_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImmopulseApplication {
	public static void main(String[] args) {
		SpringApplication.run(ImmopulseApplication.class, args);
	}
}
