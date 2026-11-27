package com.gestao.clinix.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.gestao.clinix.dto.MedicationRequest;
import com.gestao.clinix.dto.MedicationResponse;
import com.gestao.clinix.exception.GlobalExceptionHandler;
import com.gestao.clinix.exception.ResourceNotFoundException;
import com.gestao.clinix.service.MedicationOptions;
import com.gestao.clinix.service.MedicationService;

class MedicationControllerTest {
    private static final String PAYLOAD = """
            {"medicationName":"Comercial","activeIngredient":[
              {"name":"Componente","dosage":500,"unitMeasurement":"mg"}],
             "category":"ANALGESIC","presentationFormat":"TABLET","manufacturer":"Fabricante"}
            """;

    private MedicationService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(MedicationService.class);
        mvc = MockMvcBuilders.standaloneSetup(new MedicationController(service, new MedicationOptions()))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void readsKeepMedicationFields() throws Exception {
        when(service.getById(1L)).thenReturn(medication());
        mvc.perform(get("/medications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.medicationName").value("Comercial"))
                .andExpect(jsonPath("$.activeIngredient[0].name").value("Componente"));
    }

    @Test
    void paginatedSearchKeepsTypedEnvelopeAndMedicationFields() throws Exception {
        when(service.list("Com", 1, 2, "medicationName", "desc"))
                .thenReturn(new MedicationService.MedicationPage(List.of(medication()), 1, 2, 3, 2, true));
        mvc.perform(get("/medications").param("search", "Com")
                        .param("pageSize", "2").param("pageNumber", "1")
                        .param("sortBy", "medicationName").param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].quantity").value(0))
                .andExpect(jsonPath("$.content[0].category").value("ANALGESIC"))
                .andExpect(jsonPath("$.content[0].activeIngredient[0].unitMeasurement").value("mg"))
                .andExpect(jsonPath("$.content[0].status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.lastPage").value(true));
        verify(service).list("Com", 1, 2, "medicationName", "desc");
    }

    @Test
    void writesUseCurrentRoutesAndResponseContract() throws Exception {
        when(service.create(any())).thenReturn(medication());
        when(service.update(eq(1L), any())).thenReturn(medication());
        mvc.perform(post("/medications").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/medications/1"))
                .andExpect(jsonPath("$.medicationName").value("Comercial"));
        mvc.perform(put("/medications/1").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(delete("/medications/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }

    @Test
    void missingResourcesUseStandardNotFoundResponse() throws Exception {
        when(service.getById(1L)).thenThrow(new ResourceNotFoundException("Medicamento não encontrado."));
        when(service.update(eq(1L), any())).thenThrow(new ResourceNotFoundException("Medicamento não encontrado."));
        doThrow(new ResourceNotFoundException("Medicamento não encontrado.")).when(service).delete(1L);
        mvc.perform(get("/medications/1")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(put("/medications/1").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(delete("/medications/1")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void databaseFailuresUseGenericErrorWithoutInternalDetails() throws Exception {
        when(service.getById(1L)).thenThrow(new DataAccessResourceFailureException("private database detail"));
        when(service.create(any())).thenThrow(new DataAccessResourceFailureException("private database detail"));
        mvc.perform(get("/medications/1")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Nao foi possivel concluir a operacao. Tente novamente mais tarde."));
        mvc.perform(post("/medications").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    private MedicationResponse medication() {
        return new MedicationResponse(1L, "Comercial", List.of(
                new MedicationRequest.Ingredient("Componente", new BigDecimal("500"), "mg")),
                "ANALGESIC", "TABLET", "Fabricante", 0, "UNAVAILABLE",
                Instant.parse("2026-11-23T15:00:00Z"), "qa@example.invalid");
    }
}
