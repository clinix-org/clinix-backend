package com.gestao.clinix.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gestao.clinix.dto.AuthenticatedUserContextResponse;
import com.gestao.clinix.dto.UserResponse;
import com.gestao.clinix.entity.Users;

@Component
public class UserMapper {

	public UserResponse toResponse(Users user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setNome(user.getNome());
		response.setUsuario(user.getUsuario());
		response.setRole(user.getRole());
		response.setAtivo(user.isAtivo());
		response.setCreatedBy(user.getCreatedBy());
		response.setCreatedAt(user.getCreatedAt());
		response.setUpdatedBy(user.getUpdatedBy());
		response.setUpdatedAt(user.getUpdatedAt());
		return response;
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
