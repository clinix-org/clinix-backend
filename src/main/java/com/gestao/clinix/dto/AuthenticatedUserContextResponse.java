package com.gestao.clinix.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthenticatedUserContextResponse(
        Long id,
        String name,
        String email,
        String emailMasked,
        List<String> roles,
        List<String> permissions,
        String accountStatus,
        String tenant,
        String unit) {

    public AuthenticatedUserContextResponse {
        roles = roles == null ? null : List.copyOf(roles);
        permissions = permissions == null ? null : List.copyOf(permissions);
    }
}
