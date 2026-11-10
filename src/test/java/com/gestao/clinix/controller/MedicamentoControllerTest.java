package com.gestao.clinix.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    void paginatedSearchKeepsEnvelopeAndMedicationFields() throws Exception {
        var pageable = PageRequest.of(1, 2, Sort.by("nomeComercial").descending());
        Medicamento medication = medicamento();
        medication.setQuantidade(12);
        medication.setCategoria("Analgesico");
        medication.setUnidadeMedida("comprimido");
        medication.setStatus("ATIVO");
        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<Medicamento>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(medication), pageable, 3));

        mvc.perform(get("/medicamentos/nomecomercial")
                        .param("nomeComercial", "Com")
                        .param("pageSize", "2").param("pageNumber", "1")
                        .param("sortBy", "nomeComercial").param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicamentos[0].id").value(1))
                .andExpect(jsonPath("$.medicamentos[0].quantidade").value(12))
                .andExpect(jsonPath("$.medicamentos[0].categoria").value("Analgesico"))
                .andExpect(jsonPath("$.medicamentos[0].unidadeMedida").value("comprimido"))
                .andExpect(jsonPath("$.medicamentos[0].status").value("ATIVO"))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.lastPage").value(true))
                .andExpect(jsonPath("$.timestamp").exists());
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
