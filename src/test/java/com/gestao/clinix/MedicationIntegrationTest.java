package com.gestao.clinix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import java.util.List;
import java.util.concurrent.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestao.clinix.config.JwtService;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.JdbcTemplate;
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
    @Autowired JdbcTemplate jdbc;
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
        Instant before = Instant.now().minusMillis(1);
        String response = create("Medication test");
        Instant after = Instant.now().plusMillis(1);
        long id = json.readTree(response).get("id").asLong();
        var persisted = medications.findById(id).orElseThrow();
        assertThat(persisted.getCreatedAt()).isBetween(before, after);
        assertThat(persisted.getCreatedBy()).isEqualTo("medication-admin@example.test");
        assertThat(Instant.parse(json.readTree(response).get("createdAt").asText()))
                .isBetween(before, after);
        assertThat(json.readTree(response).get("quantity").asInt()).isZero();
        assertThat(json.readTree(response).get("status").asText()).isEqualTo("UNAVAILABLE");
        assertThat(json.readTree(response).get("createdBy").asText()).isEqualTo("medication-admin@example.test");
        assertThat(logs.findAll()).hasSize(1);
        var log = logs.findAll().get(0);
        assertThat(log.getAction()).isEqualTo("CREATE");
        assertThat(log.getEntityName()).isEqualTo("Medication");
        assertThat(log.getEntityId()).isEqualTo(id);
        assertThat(log.getPerformedBy()).isEqualTo(persisted.getCreatedBy());
        assertThat(log.getPerformedAt().toInstant(ZoneOffset.UTC)).isBetween(before, after);
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
        mvc.perform(post("/medications").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
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
        Instant originalCreatedAt = entity.getCreatedAt();
        String originalCreatedBy = entity.getCreatedBy();
        medications.save(entity);
        mvc.perform(put("/medications/" + id).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(
                        payload("Updated", List.of(ingredient("A", 1.25, "mg/mL"))))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(12))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
        var updated = medications.findById(id).orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getCreatedBy()).isEqualTo(originalCreatedBy);
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

    static Stream<Arguments> requiredFields() {
        return Stream.of("medicationName", "category", "presentationFormat", "manufacturer",
                        "activeIngredient[0].name", "activeIngredient[0].unitMeasurement",
                        "activeIngredient[0].dosage", "activeIngredient")
                .flatMap(field -> {
                    Stream<String> states = field.equals("activeIngredient")
                            ? Stream.of("missing", "null", "emptyList")
                            : field.endsWith("dosage") ? Stream.of("missing", "null")
                            : Stream.of("missing", "null", "empty", "blank");
                    return states.map(state -> Arguments.of(field, state));
                });
    }

    @ParameterizedTest(name = "Required {0}: {1}")
    @MethodSource("requiredFields")
    void rejectsEveryMissingOrEmptyRequiredFieldWithoutPersisting(String field, String state) throws Exception {
        var ingredient = new HashMap<>(ingredient("A", 500, "mg"));
        var body = new HashMap<>(payload("Required field test", List.of(ingredient)));
        Map<String, Object> target = field.startsWith("activeIngredient[0].") ? ingredient : body;
        String key = field.substring(field.lastIndexOf('.') + 1);
        if (state.equals("missing")) {
            target.remove(key);
        } else {
            Object value = switch (state) {
                case "null" -> null;
                case "emptyList" -> List.of();
                case "blank" -> "   ";
                default -> "";
            };
            target.put(key, value);
        }
        mvc.perform(post("/medications").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields['" + field + "']").exists());
        assertThat(medications.count()).isZero();
        assertThat(logs.count()).isZero();
    }

    static Stream<Arguments> differentCompositions() {
        return Stream.of(Arguments.of("A", 250, "mg"), Arguments.of("A", 500, "g"),
                Arguments.of("B", 500, "mg"));
    }

    @ParameterizedTest
    @MethodSource("differentCompositions")
    void allowsSameCommercialNameWhenOnlyCompositionChanges(String name, int dosage, String unit) throws Exception {
        var first = payload("Same medication", List.of(ingredient("A", 500, "mg")));
        var second = payload("Same medication", List.of(ingredient(name, dosage, unit)));
        for (var body : List.of(first, second)) {
            mvc.perform(post("/medications").header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isCreated());
        }
        assertThat(medications.count()).isEqualTo(2);
    }

    static Stream<Arguments> invalidOptions() {
        return Stream.of(Arguments.of("category"), Arguments.of("presentationFormat"));
    }

    @ParameterizedTest
    @MethodSource("invalidOptions")
    void rejectsUnknownSelectionOptions(String field) throws Exception {
        var body = new HashMap<>(payload("Invalid option", List.of(ingredient("A", 1, "mg"))));
        body.put(field, "UNKNOWN");
        mvc.perform(post("/medications").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
        assertThat(medications.count()).isZero();
        assertThat(logs.count()).isZero();
    }

    @Test
    void ignoresClientSuppliedCreationAuditAndInitialStock() throws Exception {
        var body = new HashMap<>(payload("Server metadata", List.of(ingredient("A", 1, "mg"))));
        body.put("createdBy", "another-user@example.test");
        body.put("createdAt", "2000-01-01T00:00:00Z");
        body.put("quantity", 999);
        body.put("status", "AVAILABLE");
        Instant before = Instant.now().minusMillis(1);
        String response = mvc.perform(post("/medications").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        var saved = medications.findById(json.readTree(response).get("id").asLong()).orElseThrow();
        assertThat(saved.getCreatedBy()).isEqualTo("medication-admin@example.test");
        assertThat(saved.getCreatedAt()).isBetween(before, Instant.now().plusMillis(1));
        assertThat(saved.getQuantity()).isZero();
        assertThat(saved.getStatus()).isEqualTo("UNAVAILABLE");
        assertThat(logs.findAll().get(0).getPerformedBy()).isEqualTo(saved.getCreatedBy());
    }

    @Test
    void persistsDecimalLimitsExactly() throws Exception {
        for (String dosage : List.of("0.000001", "123456789012.123456", "999999999999.999999")) {
            var body = payload("Decimal " + dosage, List.of(ingredient("A", new BigDecimal(dosage), "mg")));
            String response = mvc.perform(post("/medications").header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            long id = json.readTree(response).get("id").asLong();
            BigDecimal saved = jdbc.queryForObject(
                    "select dosage from medication_active_ingredients where medication_id = ?",
                    BigDecimal.class, id);
            assertThat(saved).isEqualByComparingTo(dosage);
        }
    }
}
