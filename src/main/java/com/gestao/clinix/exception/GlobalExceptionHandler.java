package com.gestao.clinix.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gestao.clinix.config.RequestIdFilter;
import com.gestao.clinix.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		Map<String, String> fields = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error ->
				fields.putIfAbsent(error.getField(), error.getDefaultMessage()));

		ApiErrorResponse body = new ApiErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"VALIDATION_ERROR",
				"Bad Request",
				"Confira os campos informados e tente novamente.",
				request.getRequestURI(),
				getRequestId(request),
				fields,
				null);

		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException ex,
			HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", ex.getMessage(), request);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler({ AccountLockedException.class, DisabledException.class })
	public ResponseEntity<ApiErrorResponse> handleAccountLocked(RuntimeException ex,
			HttpServletRequest request) {
		return build(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "Conta inativa ou bloqueada.", request);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex,
			HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "E-mail ou senha incorretos.", request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
			HttpServletRequest request) {
		LOGGER.warn("Data integrity violation. requestId={}, path={}", getRequestId(request), request.getRequestURI());
		return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_CONFLICT",
				"Nao foi possivel salvar. Verifique se os dados ja existem.", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		LOGGER.error("Unexpected error. requestId={}, path={}", getRequestId(request), request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
				"Nao foi possivel concluir a operacao. Tente novamente mais tarde.", request);
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message,
			HttpServletRequest request) {
		ApiErrorResponse body = new ApiErrorResponse(
				status.value(),
				code,
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				getRequestId(request));

		return ResponseEntity.status(status).body(body);
	}

	private String getRequestId(HttpServletRequest request) {
		Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
		return requestId == null ? null : requestId.toString();
	}
}
