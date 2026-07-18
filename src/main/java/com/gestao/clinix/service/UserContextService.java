package com.gestao.clinix.service;

import org.springframework.stereotype.Service;

import com.gestao.clinix.dto.AuthenticatedUserContextResponse;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.exception.AccountLockedException;
import com.gestao.clinix.exception.ResourceNotFoundException;
import com.gestao.clinix.repository.UserRepository;

@Service
public class UserContextService {

	private final UserRepository userRepository;
	private final AccessPolicyService accessPolicyService;

	public UserContextService(UserRepository userRepository, AccessPolicyService accessPolicyService) {
		this.userRepository = userRepository;
		this.accessPolicyService = accessPolicyService;
	}

	public AuthenticatedUserContextResponse getContextByUsername(String username) {
		Users user = userRepository.findByUsuario(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário da sessão não encontrado."));

		if (!user.isAtivo()) {
			throw new AccountLockedException("Conta inativa ou bloqueada.");
		}

		return new AuthenticatedUserContextResponse(
				user.getId(),
				user.getNome(),
				user.getUsuario(),
				maskEmail(user.getUsuario()),
				accessPolicyService.resolveRoles(user.getRole()),
				accessPolicyService.resolvePermissions(user.getRole()),
				"ACTIVE",
				null,
				null);
	}

	private String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return email;
		}

		String[] parts = email.split("@", 2);
		String local = parts[0];
		String domain = parts[1];

		if (local.length() <= 2) {
			return local.charAt(0) + "***@" + domain;
		}

		return local.substring(0, 2) + "***@" + domain;
	}
}
