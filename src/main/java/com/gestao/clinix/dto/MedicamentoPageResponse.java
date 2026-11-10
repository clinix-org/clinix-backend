package com.gestao.clinix.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MedicamentoPageResponse(
        List<MedicamentoResponse> medicamentos,
        Integer pageSize,
        Integer pageNumber,
        Long totalElements,
        Integer totalPages,
        boolean lastPage,
        LocalDateTime timestamp) {
}
