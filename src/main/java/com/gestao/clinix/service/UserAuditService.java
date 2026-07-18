package com.gestao.clinix.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.gestao.clinix.entity.SystemLogAction;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.security.AuthenticatedUserProvider;

@Service
public class UserAuditService {

	private static final String ENTITY_NAME = "Users";

	private final AuthenticatedUserProvider authenticatedUserProvider;
	private final SystemLogService systemLogService;

	public UserAuditService(AuthenticatedUserProvider authenticatedUserProvider, SystemLogService systemLogService) {
		this.authenticatedUserProvider = authenticatedUserProvider;
		this.systemLogService = systemLogService;
	}

	public AuditMetadata currentMetadata() {
		return new AuditMetadata(authenticatedUserProvider.getAuthenticatedUsername(), LocalDateTime.now());
	}

	public void registerCreated(Users user, AuditMetadata metadata) {
		register(SystemLogAction.CREATE, user.getId(), metadata, "Usuario criado: " + user.getUsuario());
	}

	public void registerUpdated(Users user, AuditMetadata metadata) {
		register(SystemLogAction.UPDATE, user.getId(), metadata, "Usuario atualizado: " + user.getUsuario());
	}

	public void registerDeleted(Users user, AuditMetadata metadata) {
		register(SystemLogAction.DELETE, user.getId(), metadata, "Usuario excluido: " + user.getUsuario());
	}

	private void register(SystemLogAction action, Long entityId, AuditMetadata metadata, String details) {
		systemLogService.register(action, ENTITY_NAME, entityId, metadata.performedBy(), metadata.performedAt(), details);
	}
}
