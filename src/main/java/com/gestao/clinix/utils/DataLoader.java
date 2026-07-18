package com.gestao.clinix.utils;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.gestao.clinix.entity.Users;
import com.gestao.clinix.repository.UserRepository;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true")
public class DataLoader implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final Environment environment;

	@Value("${app.bootstrap.admin.email}")
	private String adminEmail;

	@Value("${app.bootstrap.admin.password}")
	private String adminPassword;

	@Value("${app.bootstrap.admin.name}")
	private String adminName;

	@Value("${app.bootstrap.admin.active}")
	private boolean adminActive;

	public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.environment = environment;
	}

	@Override
	public void run(String... args) throws Exception {
		if (!hasText(adminEmail) || !hasText(adminPassword) || !hasText(adminName)) {
			throw new IllegalStateException(
					"Bootstrap habilitado, mas ADMIN_EMAIL, ADMIN_PASSWORD ou ADMIN_NAME nao foi definido.");
		}
		createUserIfAbsent(adminName, adminEmail, adminPassword, "ADMIN", adminActive);
		createConfiguredUsers();
	}

	private void createConfiguredUsers() {
		for (int index = 0; ; index++) {
			String prefix = "initial.users[" + index + "].";
			String email = environment.getProperty(prefix + "email");

			if (!hasText(email)) {
				break;
			}

			String password = environment.getProperty(prefix + "password");
			String name = environment.getProperty(prefix + "name");
			String role = environment.getProperty(prefix + "role", "USER");
			boolean active = environment.getProperty(prefix + "active", Boolean.class,
					environment.getProperty(prefix + "ativo", Boolean.class, true));

			if (!hasText(password) || !hasText(name)) {
				System.out.println("Usuario inicial ignorado por falta de name/password: " + email);
				continue;
			}

			createUserIfAbsent(name, email, password, role, active);
		}
	}

	private void createUserIfAbsent(String name, String email, String password, String role, boolean active) {
		userRepository.findByUsuario(email).ifPresentOrElse(existing -> {
			System.out.println("Usuario inicial ja existe: " + existing.getUsuario());
		}, () -> {
			Users user = new Users();
			user.setNome(name);
			user.setUsuario(email);
			user.setSenha(passwordEncoder.encode(password));
			user.setRole(role);
			user.setAtivo(active);
			user.setCreatedBy("SYSTEM");
			user.setCreatedAt(LocalDateTime.now());

			userRepository.save(user);
			System.out.println("Usuario inicial criado: " + user.getUsuario());
		});
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
