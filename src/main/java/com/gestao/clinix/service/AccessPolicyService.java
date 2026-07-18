package com.gestao.clinix.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AccessPolicyService {

	private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
			"ADMIN", List.of(
					"dashboard:read",
					"users:read",
					"users:create",
					"users:update",
					"users:delete",
					"medicamentos:read",
					"medicamentos:create",
					"medicamentos:update",
					"medicamentos:delete",
					"relatorios:read",
					"configuracoes:read"),
			"USER", List.of(
					"dashboard:read",
					"medicamentos:read"));

	public String normalizeRole(String role) {
		if (!StringUtils.hasText(role)) {
			return "USER";
		}

		String normalized = role.trim().toUpperCase(Locale.ROOT);
		if (normalized.startsWith("ROLE_")) {
			normalized = normalized.substring("ROLE_".length());
		}

		return normalized;
	}

	public List<String> resolveRoles(String role) {
		return List.of(normalizeRole(role));
	}

	public List<String> resolvePermissions(String role) {
		List<String> permissions = new ArrayList<>();
		for (String resolvedRole : resolveRoles(role)) {
			permissions.addAll(ROLE_PERMISSIONS.getOrDefault(resolvedRole, ROLE_PERMISSIONS.get("USER")));
		}

		return permissions.stream().distinct().sorted().toList();
	}
}
