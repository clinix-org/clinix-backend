package com.gestao.clinix.service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.gestao.clinix.dto.UserCreateRequest;
import com.gestao.clinix.dto.UserResponse;
import com.gestao.clinix.dto.UserUpdateRequest;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.exception.DuplicateResourceException;
import com.gestao.clinix.exception.ResourceNotFoundException;
import com.gestao.clinix.mapper.UserMapper;
import com.gestao.clinix.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository repo;
	private final PasswordEncoder encoder;
	private final UserMapper userMapper;
	private final AccessPolicyService accessPolicyService;
	private final UserAuditService userAuditService;

	public UserService(UserRepository repo, PasswordEncoder encoder, UserMapper userMapper,
			AccessPolicyService accessPolicyService, UserAuditService userAuditService) {
		this.repo = repo;
		this.encoder = encoder;
		this.userMapper = userMapper;
		this.accessPolicyService = accessPolicyService;
		this.userAuditService = userAuditService;
	}

	@Transactional
	public UserResponse create(UserCreateRequest dto) {
		String username = normalizeUsuario(dto.getUsuario());
		ensureUsuarioAvailableForCreate(username);

		AuditMetadata metadata = userAuditService.currentMetadata();
		Users user = buildNewUser(dto, username, metadata);

		Users saved = repo.save(user);
		userAuditService.registerCreated(saved, metadata);
		return userMapper.toResponse(saved);
	}

	@Transactional
	public UserResponse update(Long id, UserUpdateRequest dto) {
		Users user = findUserOrThrow(id);
		AuditMetadata metadata = userAuditService.currentMetadata();
		String normalizedRole = normalizeRole(dto.getRole());

		validateSelfUpdate(user, metadata, dto, normalizedRole);
		applyUpdate(user, dto, normalizedRole, metadata);

		Users saved = repo.save(user);
		userAuditService.registerUpdated(saved, metadata);
		return userMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public UserResponse findById(Long id) {
		return userMapper.toResponse(findUserOrThrow(id));
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return repo.findAll().stream().map(userMapper::toResponse).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findAll(String search) {
		if (!StringUtils.hasText(search)) {
			return findAll();
		}

		String term = search.trim();
		return repo.searchByTerm(term).stream()
				.map(userMapper::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public void delete(Long id) {
		Users user = findUserOrThrow(id);
		AuditMetadata metadata = userAuditService.currentMetadata();
		validateSelfDelete(user, metadata);

		repo.delete(user);
		userAuditService.registerDeleted(user, metadata);
	}

	private Users buildNewUser(UserCreateRequest dto, String username, AuditMetadata metadata) {
		Users user = new Users();
		user.setNome(dto.getNome().trim());
		user.setUsuario(username);
		user.setSenha(encoder.encode(dto.getSenha()));
		user.setRole(normalizeRole(dto.getRole()));
		user.setAtivo(true);
		user.setCreatedBy(metadata.performedBy());
		user.setCreatedAt(metadata.performedAt());
		return user;
	}

	private void applyUpdate(Users user, UserUpdateRequest dto, String normalizedRole, AuditMetadata metadata) {
		String username = normalizeUsuario(dto.getUsuario());
		ensureUsuarioAvailableForUpdate(user, username);

		user.setUsuario(username);
		user.setNome(dto.getNome().trim());
		if (StringUtils.hasText(dto.getSenha())) {
			user.setSenha(encoder.encode(dto.getSenha()));
		}
		user.setRole(normalizedRole);
		user.setAtivo(dto.isAtivo());
		user.setUpdatedBy(metadata.performedBy());
		user.setUpdatedAt(metadata.performedAt());
	}

	private void validateSelfUpdate(Users user, AuditMetadata metadata, UserUpdateRequest dto, String normalizedRole) {
		if (!isAuthenticatedUser(user, metadata)) {
			return;
		}

		if (!dto.isAtivo()) {
			throw new IllegalArgumentException("Nao e permitido desativar o proprio usuario autenticado.");
		}

		if (!"ADMIN".equals(normalizedRole)) {
			throw new IllegalArgumentException("Nao e permitido remover a permissao ADMIN do proprio usuario autenticado.");
		}
	}

	private void validateSelfDelete(Users user, AuditMetadata metadata) {
		if (isAuthenticatedUser(user, metadata)) {
			throw new IllegalArgumentException("Nao e permitido excluir o proprio usuario autenticado.");
		}
	}

	private boolean isAuthenticatedUser(Users user, AuditMetadata metadata) {
		return user.getUsuario() != null && user.getUsuario().equalsIgnoreCase(metadata.performedBy());
	}

	private void ensureUsuarioAvailableForCreate(String username) {
		repo.findByUsuario(username).ifPresent(u -> {
			throw new DuplicateResourceException("Ja existe um usuario cadastrado com este e-mail.");
		});
	}

	private void ensureUsuarioAvailableForUpdate(Users user, String username) {
		if (user.getUsuario().equalsIgnoreCase(username)) {
			return;
		}

		repo.findByUsuario(username).ifPresent(other -> {
			throw new DuplicateResourceException("Outro usuario ja utiliza este e-mail.");
		});
	}

	private Users findUserOrThrow(Long id) {
		return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado."));
	}

	private String normalizeUsuario(String username) {
		return username.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeRole(String role) {
		return accessPolicyService.normalizeRole(role);
	}

}
