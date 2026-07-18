package com.gestao.clinix.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") // ou "USERS" se você tiver criado com aspas; mas provavelmente 'users'
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false, length = 255)
	private String nome;

	// Mapeia para a coluna 'username' existente no banco
	@Column(name = "username", nullable = false, unique = true, length = 255)
	private String usuario;

	// Mapeia para a coluna 'password' existente no banco
	@Column(name = "password", nullable = false, length = 255)
	private String senha;

	@Column(name = "role", nullable = false, length = 50)
	private String role;

	// Se no banco já existe a coluna 'ativo' então mapeamos para ela
	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	@Column(name = "created_by", length = 255)
	private String createdBy = "SYSTEM";

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_by", length = 255)
	private String updatedBy;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// getters e setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
