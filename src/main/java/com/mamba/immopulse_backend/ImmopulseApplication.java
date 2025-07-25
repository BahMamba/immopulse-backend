package com.mamba.immopulse_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mamba.immopulse_backend.model.entity.User;
import com.mamba.immopulse_backend.model.enums.RoleUser;
import com.mamba.immopulse_backend.repository.UserRepository;

@SpringBootApplication
public class ImmopulseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImmopulseApplication.class, args);
	}

}
