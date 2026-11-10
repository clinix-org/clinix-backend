package com.gestao.clinix.dto;

import java.time.LocalDateTime;

/** Resposta de usuario, sem credenciais. */
public record UserResponse(
        Long id,
        String nome,
        String usuario,
        String role,
        boolean ativo,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt) {
}
