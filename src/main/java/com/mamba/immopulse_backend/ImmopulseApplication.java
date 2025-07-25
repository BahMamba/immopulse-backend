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


	@Bean
	public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			String adminEmail = "admin@immopulse.com";

			// Vérifie si l'admin existe déjà
			if (userRepository.findByEmail(adminEmail).isEmpty()) {
				User admin = new User();
				admin.setFullname("Darty Admin");
				admin.setEmail(adminEmail);
				admin.setPassword(passwordEncoder.encode("1234")); // Mot de passe hashé
				admin.setRoleUser(RoleUser.ADMIN);
				admin.set_active(true);

				userRepository.save(admin);
				System.out.println("✅ Admin créé avec succès (email: admin@immopulse.com, mot de passe: admin123)");
			} else {
				System.out.println("ℹ️ Admin déjà existant. Aucun changement.");
			}
		};
	}
}
