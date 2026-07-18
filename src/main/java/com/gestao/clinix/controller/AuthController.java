package com.gestao.clinix.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestao.clinix.config.JwtService;
import com.gestao.clinix.dto.AuthenticatedUserContextResponse;
import com.gestao.clinix.dto.LoginRequest;
import com.gestao.clinix.dto.LoginResponse;
import com.gestao.clinix.exception.AccountLockedException;
import com.gestao.clinix.security.AuthenticatedUserProvider;
import com.gestao.clinix.service.UserContextService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final AuthenticatedUserProvider authenticatedUserProvider;
	private final UserContextService userContextService;

	public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
			AuthenticatedUserProvider authenticatedUserProvider, UserContextService userContextService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.authenticatedUserProvider = authenticatedUserProvider;
		this.userContextService = userContextService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			String token = jwtService.generateToken(request.getUsername());
			return new LoginResponse(token);
		} catch (DisabledException e) {
			throw new AccountLockedException("Conta inativa ou bloqueada.");
		} catch (RuntimeException e) {
			throw new BadCredentialsException("E-mail ou senha incorretos.", e);
		}
	}

	@GetMapping("/me")
	public AuthenticatedUserContextResponse me() {
		String username = authenticatedUserProvider.getAuthenticatedUsername();
		return userContextService.getContextByUsername(username);
	}
}
