package com.gestao.clinix.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record MedicationRequest(
        @NotBlank(message = "Informe o nome do medicamento.")
        @Size(max = 200, message = "O nome deve ter até 200 caracteres.") String medicationName,
        @NotEmpty(message = "Informe pelo menos um princípio ativo.")
        @Size(max = 30, message = "Informe no máximo 30 princípios ativos.")
        List<@NotNull(message = "Princípio ativo inválido.") @Valid Ingredient> activeIngredient,
        @NotBlank(message = "Selecione a categoria.") @Size(max = 50) String category,
        @NotBlank(message = "Selecione a forma de apresentação.") @Size(max = 50) String presentationFormat,
        @NotBlank(message = "Informe o fabricante.")
        @Size(max = 200, message = "O fabricante deve ter até 200 caracteres.") String manufacturer) {

    public record Ingredient(
            @NotBlank(message = "Informe o nome do princípio ativo.")
            @Size(max = 200, message = "O princípio ativo deve ter até 200 caracteres.") String name,
            @NotNull(message = "Informe a dosagem.")
            @DecimalMin(value = "0", inclusive = false, message = "A dosagem deve ser maior que zero.")
            @Digits(integer = 12, fraction = 6,
                    message = "A dosagem aceita até 12 dígitos inteiros e 6 decimais.") BigDecimal dosage,
            @NotBlank(message = "Selecione a unidade de medida.") @Size(max = 20) String unitMeasurement) {}
}
