package com.gestao.clinix.dto;

import java.time.Instant;
import java.util.List;

public record MedicationResponse(Long id, String medicationName,
        List<MedicationRequest.Ingredient> activeIngredient, String category,
        String presentationFormat, String manufacturer, int quantity, String status,
        Instant createdAt, String createdBy) {}
