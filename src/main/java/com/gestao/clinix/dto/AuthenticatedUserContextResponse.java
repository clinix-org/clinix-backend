package com.gestao.clinix.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticatedUserContextResponse {

	private Long id;
	private String name;
	private String email;
	private String emailMasked;
	private List<String> roles;
	private List<String> permissions;
	private String accountStatus;
	private String tenant;
	private String unit;

	public AuthenticatedUserContextResponse(Long id, String name, String email, String emailMasked,
			List<String> roles, List<String> permissions, String accountStatus, String tenant, String unit) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.emailMasked = emailMasked;
		this.roles = roles;
		this.permissions = permissions;
		this.accountStatus = accountStatus;
		this.tenant = tenant;
		this.unit = unit;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getEmailMasked() {
		return emailMasked;
	}

	public List<String> getRoles() {
		return roles;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public String getAccountStatus() {
		return accountStatus;
	}

	public String getTenant() {
		return tenant;
	}

	public String getUnit() {
		return unit;
	}
}
