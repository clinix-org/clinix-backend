package com.gestao.clinix.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestao.clinix.config.CustomAccessDeniedHandler;
import com.gestao.clinix.config.CustomAuthenticationEntryPoint;
import com.gestao.clinix.config.RequestIdFilter;
import com.gestao.clinix.entity.Users;
import com.gestao.clinix.exception.GlobalExceptionHandler;
import com.gestao.clinix.mapper.MedicamentoMapper;
import com.gestao.clinix.mapper.UserMapper;

@JsonTest
class ResponseContractTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginResponseKeepsTokenProperty() {
        assertThat(objectMapper.<JsonNode>valueToTree(new LoginResponse("test-token")))
                .isEqualTo(objectMapper.createObjectNode().put("token", "test-token"));
    }

    @Test
    void userResponseKeepsJsonFieldsAndOmitsPassword() throws Exception {
        Users user = new Users();
        user.setId(7L);
        user.setNome("Maria");
        user.setUsuario("maria@clinix.com");
        user.setSenha("encoded-password");
        user.setRole("USER");
        user.setAtivo(true);
        user.setCreatedBy("SYSTEM");
        user.setCreatedAt(LocalDateTime.of(2026, 10, 19, 12, 0));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new UserMapper().toResponse(user)));

        assertThat(json).isEqualTo(objectMapper.readTree("""
                {"id":7,"nome":"Maria","usuario":"maria@clinix.com","role":"USER",
                 "ativo":true,"createdBy":"SYSTEM","createdAt":"2026-10-19T12:00:00",
                 "updatedBy":null,"updatedAt":null}
                """));
    }

    @Test
    void medicamentoInputAndRecordResponseKeepTheSameJsonShape() throws Exception {
        String payload = """
                {"id":9,"nomeComercial":"Comercial","nomeGenerico":"Generico"}
                """;
        MedicamentoDTO request = objectMapper.readValue(payload, MedicamentoDTO.class);
        MedicamentoMapper mapper = new MedicamentoMapper();

        JsonNode response = objectMapper.readTree(
                objectMapper.writeValueAsString(mapper.toResponse(mapper.toEntity(request))));

        assertThat(response).isEqualTo(objectMapper.readTree(payload));
    }

    @Test
    void contextCopiesCollectionsAndKeepsNullFieldsOmitted() throws Exception {
        List<String> roles = new ArrayList<>(List.of("USER"));
        List<String> permissions = new ArrayList<>(List.of("medicamentos:read"));
        var response = new AuthenticatedUserContextResponse(7L, "Maria", "maria@clinix.com",
                "ma***@clinix.com", roles, permissions, "ACTIVE", null, null);

        roles.add("ADMIN");
        permissions.clear();

        assertThatThrownBy(() -> response.roles().add("ADMIN"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> response.permissions().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(response)))
                .isEqualTo(objectMapper.readTree("""
                {"id":7,"name":"Maria","email":"maria@clinix.com","emailMasked":"ma***@clinix.com",
                 "roles":["USER"],"permissions":["medicamentos:read"],"accountStatus":"ACTIVE"}
                """));
    }

    @Test
    void securityAndMvcErrorsKeepTheirPublicCodes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, "request-123");
        MockHttpServletResponse unauthorized = new MockHttpServletResponse();
        new CustomAuthenticationEntryPoint(objectMapper).commence(request, unauthorized,
                new BadCredentialsException("invalid"));
        MockHttpServletResponse forbidden = new MockHttpServletResponse();
        new CustomAccessDeniedHandler(objectMapper).handle(request, forbidden,
                new AccessDeniedException("denied"));

        assertThat(unauthorized.getStatus()).isEqualTo(401);
        JsonNode unauthorizedJson = objectMapper.readTree(unauthorized.getContentAsString());
        assertThat(unauthorizedJson.get("code").asText()).isEqualTo("AUTH_UNAUTHORIZED");
        assertThat(unauthorizedJson.get("requestId").asText()).isEqualTo("request-123");
        assertThat(forbidden.getStatus()).isEqualTo(403);
        assertThat(objectMapper.readTree(forbidden.getContentAsString()).get("code").asText())
                .isEqualTo("AUTH_FORBIDDEN");
        var mvcResponse = new GlobalExceptionHandler().handleAuthentication(
                new BadCredentialsException("invalid"), request);
        assertThat(mvcResponse.getBody().getCode()).isEqualTo("AUTH_UNAUTHORIZED");
        assertThat(mvcResponse.getBody().getMessage()).isEqualTo("E-mail ou senha incorretos.");
    }
}
