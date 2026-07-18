package com.gestao.clinix.service;

import java.time.LocalDateTime;

public record AuditMetadata(String performedBy, LocalDateTime performedAt) {
}
