package com.gestao.clinix.service;

import org.springframework.stereotype.Service;

import com.gestao.clinix.dto.AuthenticatedUserContextResponse;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.exception.AccountLockedException;
import com.gestao.clinix.exception.ResourceNotFoundException;
import com.gestao.clinix.mapper.UserMapper;
import com.gestao.clinix.repository.UserRepository;

@Service
public class UserContextService {

	private final UserRepository userRepository;
	private final AccessPolicyService accessPolicyService;
	private final UserMapper userMapper;

	public UserContextService(UserRepository userRepository, AccessPolicyService accessPolicyService,
			UserMapper userMapper) {
		this.userRepository = userRepository;
		this.accessPolicyService = accessPolicyService;
		this.userMapper = userMapper;
	}

	public AuthenticatedUserContextResponse getContextByUsername(String username) {
		Users user = userRepository.findByUsuario(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário da sessão não encontrado."));

		if (!user.isAtivo()) {
			throw new AccountLockedException("Conta inativa ou bloqueada.");
		}

		return userMapper.toContextResponse(user,
				accessPolicyService.resolveRoles(user.getRole()),
				accessPolicyService.resolvePermissions(user.getRole()), "ACTIVE");
	}
}
