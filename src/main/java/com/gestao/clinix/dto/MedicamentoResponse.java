package com.gestao.clinix.dto;

public record MedicamentoResponse(Long id, String nomeComercial, String nomeGenerico,
        int quantidade, String unidadeMedida, String categoria, String status) {
}
