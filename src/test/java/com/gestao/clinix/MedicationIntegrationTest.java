package com.gestao.clinix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import java.util.List;
import java.util.concurrent.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestao.clinix.config.JwtService;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MedicationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JwtService jwt;
    @Autowired UserRepository users;
    @Autowired MedicationRepository medications;
    @Autowired SystemLogRepository logs;
    String admin;
    String reader;

    @BeforeEach
    void setup() {
        medications.deleteAll();
        logs.deleteAll();
        admin = token("medication-admin@example.test", "ADMIN");
        reader = token("medication-reader@example.test", "USER");
    }

    private String token(String email, String role) {
        if (users.findByUsuario(email).isEmpty()) {
            Users user = new Users();
            user.setNome("Test medication user");
            user.setUsuario(email);
            user.setSenha("unused-test-password");
            user.setRole(role);
            users.save(user);
        }
        return "Bearer " + jwt.generateToken(email);
    }

    private Map<String, Object> ingredient(String name, Number dosage, String unit) {
        return Map.of("name", name, "dosage", dosage, "unitMeasurement", unit);
    }

    private Map<String, Object> payload(String name, List<Map<String, Object>> ingredients) {
        return Map.of("medicationName", name, "activeIngredient", ingredients,
                "category", "ANALGESIC", "presentationFormat", "TABLET", "manufacturer", "Test lab");
    }

    private String create(String name) throws Exception {
        return mvc.perform(post("/medications").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(
                        payload(name, List.of(ingredient("Ingredient A", 500, "mg"))))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    @Test
    void createsListsAndAuditsWithZeroStock() throws Exception {
        String response = create("Medication test");
        assertThat(json.readTree(response).get("quantity").asInt()).isZero();
        assertThat(json.readTree(response).get("status").asText()).isEqualTo("UNAVAILABLE");
        assertThat(json.readTree(response).get("createdBy").asText()).isEqualTo("medication-admin@example.test");
        assertThat(logs.findAll()).hasSize(1);
        mvc.perform(get("/medications").param("search", "medication")
                .header("Authorization", reader))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].activeIngredient[0].dosage").value(500));
    }

    @Test
    void rejectsEquivalentCompositionRegardlessOfOrderManufacturerAndPresentation() throws Exception {
        var first = payload("  Medication   Combo  ", List.of(
                ingredient("Ingredient A", 500, "mg"), ingredient("Ingredient B", 0.25, "g")));
        mvc.perform(post("/medications").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(first)))
                .andExpect(status().isCreated());
        var second = new java.util.HashMap<>(payload("medication combo", List.of(
                ingredient("ingredient b", 0.250000, "g"), ingredient("INGREDIENT A", 500.0, "mg"))));
        second.put("manufacturer", "Another lab");
        second.put("presentationFormat", "CAPSULE");
        mvc.perform(post("/medications").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(second)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Medicamento já cadastrado."));
        assertThat(medications.count()).isEqualTo(1);
        assertThat(logs.count()).isEqualTo(1);
        mvc.perform(post("/medications").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(
                        payload("medication combo", List.of(ingredient("Ingredient A", 250, "mg"))))))
                .andExpect(status().isCreated());
    }

    @Test
    void validatesDosageCompositionOptionsAndMalformedJson() throws Exception {
        for (Number value : List.of(0, -1, 0.0000001)) {
            mvc.perform(post("/medications").header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(
                            payload("Invalid", List.of(ingredient("A", value, "mg"))))))
                    .andExpect(status().isBadRequest());
        }
        for (var body : List.of(payload(" ", List.of()), payload("Invalid", List.of(
                ingredient("A", 1, "invalid"))), payload("Invalid", List.of(
                ingredient("A", 1, "mg"), ingredient(" a ", 2, "g"))))) {
            mvc.perform(post("/medications").header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/medications").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        assertThat(medications.count()).isZero();
        assertThat(logs.count()).isZero();
    }

    @Test
    void enforcesAuthenticationAndAdminOnAllWrites() throws Exception {
        mvc.perform(get("/medications")).andExpect(status().isUnauthorized());
        String body = json.writeValueAsString(payload("Denied", List.of(ingredient("A", 1, "mg"))));
        mvc.perform(post("/medications").header("Authorization", reader)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(put("/medications/1").header("Authorization", reader)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(delete("/medications/1").header("Authorization", reader))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medications/options").header("Authorization", reader))
                .andExpect(status().isOk()).andExpect(jsonPath("$.units.length()").value(10));
    }

    @Test
    void preservesStockAndAuditOnUpdateAndSupportsDelete() throws Exception {
        long id = json.readTree(create("Original")).get("id").asLong();
        var entity = medications.findById(id).orElseThrow();
        entity.setQuantity(12);
        entity.setStatus("AVAILABLE");
        medications.save(entity);
        mvc.perform(put("/medications/" + id).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(
                        payload("Updated", List.of(ingredient("A", 1.25, "mg/mL"))))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(12))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
        mvc.perform(delete("/medications/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/medications/" + id).header("Authorization", reader))
                .andExpect(status().isNotFound());
        assertThat(logs.count()).isEqualTo(3);
    }

    @Test
    void validatesPaginationAndTreatsWildcardsAsLiteral() throws Exception {
        create("Medication test");
        for (String query : List.of("%", "_")) {
            mvc.perform(get("/medications").param("search", query).header("Authorization", reader))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        }
        mvc.perform(get("/medications").param("pageSize", "101").header("Authorization", reader))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/medications").param("sortBy", "unknown").header("Authorization", reader))
                .andExpect(status().isBadRequest());
    }

    @Test
    void simultaneousCreatesOnlyPersistOneMedicationAndOneAudit() throws Exception {
        String body = json.writeValueAsString(payload("Concurrent", List.of(ingredient("A", 1, "mg"))));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> submit = () -> {
            start.await();
            return mvc.perform(post("/medications").header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andReturn().getResponse().getStatus();
        };
        try {
            Future<Integer> first = executor.submit(submit);
            Future<Integer> second = executor.submit(submit);
            start.countDown();
            assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(medications.count()).isEqualTo(1);
            assertThat(logs.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
