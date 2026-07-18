package com.gestao.clinix.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

	@NotBlank(message = "O e-mail é obrigatório.")
	private String username;

	@NotBlank(message = "A senha é obrigatória.")
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
