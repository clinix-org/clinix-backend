package com.gestao.clinix.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gestao.clinix.dto.AuthenticatedUserContextResponse;
import com.gestao.clinix.dto.UserResponse;
import com.gestao.clinix.entity.Users;

@Component
public class UserMapper {

	public UserResponse toResponse(Users user) {
		return new UserResponse(
				user.getId(), user.getNome(), user.getUsuario(), user.getRole(), user.isAtivo(),
				user.getCreatedBy(), user.getCreatedAt(), user.getUpdatedBy(), user.getUpdatedAt());
	}

	public AuthenticatedUserContextResponse toContextResponse(Users user, List<String> roles,
			List<String> permissions, String accountStatus) {
		return new AuthenticatedUserContextResponse(
				user.getId(), user.getNome(), user.getUsuario(), maskEmail(user.getUsuario()),
				roles, permissions, accountStatus, null, null);
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
