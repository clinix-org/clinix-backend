package com.gestao.clinix.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessPolicyServiceTest {

	private final AccessPolicyService service = new AccessPolicyService();

	@Test
	void normalizeRoleRemovesSpringPrefix() {
		assertThat(service.normalizeRole("ROLE_ADMIN")).isEqualTo("ADMIN");
	}

	@Test
	void adminReceivesCriticalPermissions() {
		assertThat(service.resolvePermissions("ADMIN"))
				.contains("users:read", "users:create", "medicamentos:delete", "configuracoes:read");
	}

	@Test
	void regularUserDoesNotReceiveAdministrativePermissions() {
		assertThat(service.resolvePermissions("USER"))
				.contains("dashboard:read", "medicamentos:read")
				.doesNotContain("users:delete", "configuracoes:read");
	}

	@Test
	void blankRoleFallsBackToUserProfile() {
		assertThat(service.resolveRoles(" ")).containsExactly("USER");
	}
}
