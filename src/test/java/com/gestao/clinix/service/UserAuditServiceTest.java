package com.gestao.clinix.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gestao.clinix.entity.SystemLogAction;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.security.AuthenticatedUserProvider;

@ExtendWith(MockitoExtension.class)
class UserAuditServiceTest {

	@Mock
	private AuthenticatedUserProvider authenticatedUserProvider;

	@Mock
	private SystemLogService systemLogService;

	@Test
	void currentMetadataUsesAuthenticatedUser() {
		when(authenticatedUserProvider.getAuthenticatedUsername()).thenReturn("admin@clinix.com");
		UserAuditService service = new UserAuditService(authenticatedUserProvider, systemLogService);

		AuditMetadata metadata = service.currentMetadata();

		assertThat(metadata.performedBy()).isEqualTo("admin@clinix.com");
		assertThat(metadata.performedAt()).isNotNull();
	}

	@Test
	void registerCreatedUsesStructuredAction() {
		UserAuditService service = new UserAuditService(authenticatedUserProvider, systemLogService);
		AuditMetadata metadata = new AuditMetadata("admin@clinix.com", LocalDateTime.of(2026, 6, 24, 21, 30));
		Users user = new Users();
		user.setId(10L);
		user.setUsuario("novo@clinix.com");

		service.registerCreated(user, metadata);

		verify(systemLogService).register(eq(SystemLogAction.CREATE), eq("Users"), eq(10L), eq("admin@clinix.com"),
				any(LocalDateTime.class), eq("Usuario criado: novo@clinix.com"));
	}
}
