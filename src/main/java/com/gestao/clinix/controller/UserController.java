package com.gestao.clinix.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestao.clinix.dto.UserCreateRequest;
import com.gestao.clinix.dto.UserResponse;
import com.gestao.clinix.dto.UserUpdateRequest;
import com.gestao.clinix.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService svc;

	public UserController(UserService svc) {
		this.svc = svc;
	}

	/**
	 * Cria um novo usuário. (Proteja este endpoint com role ADMIN no
	 * SecurityConfig)
	 */
	@PostMapping
	public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest dto) {
		UserResponse created = svc.create(dto);
		// retorna 201 com location para o recurso criado
		return ResponseEntity.created(URI.create("/users/" + created.getId())).body(created);
	}

	/**
	 * Lista todos os usuários.
	 */
	@GetMapping
	public ResponseEntity<List<UserResponse>> list(@RequestParam(required = false) String search) {
		List<UserResponse> list = svc.findAll(search);
		return ResponseEntity.ok(list);
	}

	/**
	 * Busca usuário por id.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
		UserResponse r = svc.findById(id);
		return ResponseEntity.ok(r);
	}

	/**
	 * Atualiza um usuário existente.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest dto) {
		UserResponse updated = svc.update(id, dto);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Deleta um usuário.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		svc.delete(id);
		return ResponseEntity.noContent().build();
	}
}
