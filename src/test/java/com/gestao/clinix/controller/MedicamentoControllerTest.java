package com.gestao.clinix.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.gestao.clinix.entity.Medicamento;
import com.gestao.clinix.exception.GlobalExceptionHandler;
import com.gestao.clinix.mapper.MedicamentoMapper;
import com.gestao.clinix.repository.MedicamentoRepository;
import com.gestao.clinix.service.MedicamentoService;

class MedicamentoControllerTest {

    private static final String PAYLOAD = """
            {"id":1,"nomeComercial":"Comercial","nomeGenerico":"Generico"}
            """;

    private MedicamentoRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository = mock(MedicamentoRepository.class);
        var service = new MedicamentoService(repository, new MedicamentoMapper());
        mvc = MockMvcBuilders.standaloneSetup(new MedicamentoController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void readsKeepResponseFields() throws Exception {
        Medicamento medicamento = medicamento();
        when(repository.findAll()).thenReturn(List.of(medicamento));
        when(repository.findById(1L)).thenReturn(Optional.of(medicamento));

        mvc.perform(get("/medicamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nomeComercial").value("Comercial"));
        mvc.perform(get("/medicamentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomeGenerico").value("Generico"));
    }

    @Test
    void writesKeepRoutesAndSuccessMessages() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(medicamento()));

        mvc.perform(post("/medicamentos").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(content().string("Medicamento cadastrado com sucesso!"));
        mvc.perform(put("/medicamentos").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(content().string("Medicamento alterado com sucesso!"));
        mvc.perform(delete("/medicamentos").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Medicamento deletado com sucesso!"));
        verify(repository).deleteById(1L);
    }

    @Test
    void missingResourcesUseStandardNotFoundResponse() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        mvc.perform(get("/medicamentos/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(put("/medicamentos").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(delete("/medicamentos").param("id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void databaseFailuresUseGenericErrorWithoutInternalDetails() throws Exception {
        when(repository.findAll()).thenThrow(new DataAccessResourceFailureException("private database detail"));
        when(repository.save(any(Medicamento.class)))
                .thenThrow(new DataAccessResourceFailureException("private database detail"));

        mvc.perform(get("/medicamentos"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Nao foi possivel concluir a operacao. Tente novamente mais tarde."));
        mvc.perform(post("/medicamentos").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    private Medicamento medicamento() {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(1L);
        medicamento.setNomeComercial("Comercial");
        medicamento.setNomeGenerico("Generico");
        return medicamento;
    }
}
