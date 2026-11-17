package com.gestao.clinix.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.gestao.clinix.config.JwtService;
import com.gestao.clinix.exception.GlobalExceptionHandler;
import com.gestao.clinix.security.AuthenticatedUserProvider;
import com.gestao.clinix.service.UserContextService;

class AuthControllerTest {

    private static final String LOGIN = """
            {"username":"admin@clinix.com","password":"123456"}
            """;

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        AuthController controller = new AuthController(authenticationManager, jwtService,
                mock(AuthenticatedUserProvider.class), mock(UserContextService.class));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void successfulLoginKeepsTokenResponse() throws Exception {
        when(jwtService.generateToken("admin@clinix.com")).thenReturn("test-token");

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void badCredentialsRemainUnauthorized() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("E-mail ou senha incorretos."));
        verifyNoInteractions(jwtService);
    }

    @Test
    void disabledAccountKeepsLockedResponse() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
        verifyNoInteractions(jwtService);
    }

    @ParameterizedTest
    @MethodSource("internalFailures")
    void internalAuthenticationFailuresReturnServerError(RuntimeException failure) throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(failure);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Nao foi possivel concluir a operacao. Tente novamente mais tarde."));
        verifyNoInteractions(jwtService);
    }

    static Stream<RuntimeException> internalFailures() {
        return Stream.of(
                new IllegalStateException("internal detail"),
                new DataAccessResourceFailureException("database detail"),
                new InternalAuthenticationServiceException("wrapped database detail",
                        new DataAccessResourceFailureException("database detail")));
    }

    @Test
    void tokenGenerationFailureDoesNotBecomeBadCredentials() throws Exception {
        when(jwtService.generateToken("admin@clinix.com")).thenThrow(new IllegalStateException("token detail"));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }
}
