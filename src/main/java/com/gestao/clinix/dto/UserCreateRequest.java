package com.gestao.clinix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserCreateRequest {

	@NotBlank(message = "O nome é obrigatório.")
	@Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
	private String nome;

	@NotBlank(message = "O e-mail é obrigatório.")
	@Email(message = "Informe um e-mail válido.")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@clinix\\.com$", message = "O e-mail deve pertencer ao domínio @clinix.com.")
	private String usuario;

	@NotBlank(message = "A senha é obrigatória.")
	@Pattern(regexp = "^[0-9]{6}$", message = "A senha deve conter exatamente 6 dígitos numéricos.")
	private String senha;

	@NotBlank(message = "A permissão é obrigatória.")
	@Pattern(regexp = "^(ROLE_)?(ADMIN|USER)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "A permissão deve ser ADMIN ou USER.")
	@Size(max = 50, message = "A permissão deve ter no máximo 50 caracteres.")
	private String role;

	private boolean ativo;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
}
