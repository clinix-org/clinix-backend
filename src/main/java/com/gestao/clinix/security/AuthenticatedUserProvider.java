package com.gestao.clinix.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

	public String getAuthenticatedUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AuthenticationCredentialsNotFoundException("Credencial ausente ou inválida.");
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof UserDetails userDetails) {
			return userDetails.getUsername();
		}

		if (principal instanceof String username && !username.isBlank() && !"anonymousUser".equals(username)) {
			return username;
		}

		throw new AuthenticationCredentialsNotFoundException("Credencial ausente ou inválida.");
	}
}
