package com.gestao.clinix.service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
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
import com.gestao.clinix.repository.UserRepository;

@Service
public class UserService {

	private static final String USER_NOT_FOUND_MESSAGE = "Usuario nao encontrado.";
	private static final String DUPLICATE_CREATE_MESSAGE = "Ja existe um usuario cadastrado com este e-mail.";
	private static final String DUPLICATE_UPDATE_MESSAGE = "Outro usuario ja utiliza este e-mail.";
	private static final String SELF_DELETE_MESSAGE = "Nao e permitido excluir o proprio usuario autenticado.";
	private static final String SELF_DISABLE_MESSAGE = "Nao e permitido desativar o proprio usuario autenticado.";
	private static final String SELF_ROLE_DOWNGRADE_MESSAGE =
			"Nao e permitido remover a permissao ADMIN do proprio usuario autenticado.";

	private final UserRepository repo;
	private final PasswordEncoder encoder;
	private final ModelMapper mapper;
	private final AccessPolicyService accessPolicyService;
	private final UserAuditService userAuditService;

	public UserService(UserRepository repo, PasswordEncoder encoder, ModelMapper mapper,
			AccessPolicyService accessPolicyService, UserAuditService userAuditService) {
		this.repo = repo;
		this.encoder = encoder;
		this.mapper = mapper;
		this.accessPolicyService = accessPolicyService;
		this.userAuditService = userAuditService;
	}

	@Transactional
	public UserResponse create(UserCreateRequest dto) {
		String usuario = normalizeUsuario(dto.getUsuario());
		ensureUsuarioAvailableForCreate(usuario);

		AuditMetadata metadata = userAuditService.currentMetadata();
		Users user = buildNewUser(dto, usuario, metadata);

		Users saved = repo.save(user);
		userAuditService.registerCreated(saved, metadata);
		return toResponse(saved);
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
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public UserResponse findById(Long id) {
		return toResponse(findUserOrThrow(id));
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findAll(String search) {
		if (!StringUtils.hasText(search)) {
			return findAll();
		}

		String term = search.trim();
		return repo.searchByTerm(term).stream()
				.map(this::toResponse)
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

	private Users buildNewUser(UserCreateRequest dto, String usuario, AuditMetadata metadata) {
		Users user = new Users();
		user.setNome(dto.getNome().trim());
		user.setUsuario(usuario);
		user.setSenha(encoder.encode(dto.getSenha()));
		user.setRole(normalizeRole(dto.getRole()));
		user.setAtivo(true);
		user.setCreatedBy(metadata.performedBy());
		user.setCreatedAt(metadata.performedAt());
		return user;
	}

	private void applyUpdate(Users user, UserUpdateRequest dto, String normalizedRole, AuditMetadata metadata) {
		String usuario = normalizeUsuario(dto.getUsuario());
		ensureUsuarioAvailableForUpdate(user, usuario);

		user.setUsuario(usuario);
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
			throw new IllegalArgumentException(SELF_DISABLE_MESSAGE);
		}

		if (!"ADMIN".equals(normalizedRole)) {
			throw new IllegalArgumentException(SELF_ROLE_DOWNGRADE_MESSAGE);
		}
	}

	private void validateSelfDelete(Users user, AuditMetadata metadata) {
		if (isAuthenticatedUser(user, metadata)) {
			throw new IllegalArgumentException(SELF_DELETE_MESSAGE);
		}
	}

	private boolean isAuthenticatedUser(Users user, AuditMetadata metadata) {
		return user.getUsuario() != null && user.getUsuario().equalsIgnoreCase(metadata.performedBy());
	}

	private void ensureUsuarioAvailableForCreate(String usuario) {
		repo.findByUsuario(usuario).ifPresent(u -> {
			throw new DuplicateResourceException(DUPLICATE_CREATE_MESSAGE);
		});
	}

	private void ensureUsuarioAvailableForUpdate(Users user, String usuario) {
		if (user.getUsuario().equalsIgnoreCase(usuario)) {
			return;
		}

		repo.findByUsuario(usuario).ifPresent(other -> {
			throw new DuplicateResourceException(DUPLICATE_UPDATE_MESSAGE);
		});
	}

	private Users findUserOrThrow(Long id) {
		return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
	}

	private String normalizeUsuario(String usuario) {
		return usuario.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeRole(String role) {
		return accessPolicyService.normalizeRole(role);
	}

	private UserResponse toResponse(Users user) {
		return mapper.map(user, UserResponse.class);
	}
}
