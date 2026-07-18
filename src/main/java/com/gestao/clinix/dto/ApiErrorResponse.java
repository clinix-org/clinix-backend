package com.gestao.clinix.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

	private Instant timestamp = Instant.now();
	private int status;
	private String code;
	private String error;
	private String message;
	private String path;
	private String requestId;
	private Map<String, String> fields;
	private Map<String, Object> details;

	public ApiErrorResponse() {
	}

	public ApiErrorResponse(int status, String error, String message, String path) {
		this(status, error, error, message, path, null, null, null);
	}

	public ApiErrorResponse(int status, String error, String message, String path, Map<String, String> fields) {
		this(status, error, error, message, path, null, fields, null);
	}

	public ApiErrorResponse(int status, String code, String error, String message, String path, String requestId) {
		this(status, code, error, message, path, requestId, null, null);
	}

	public ApiErrorResponse(int status, String code, String error, String message, String path, String requestId,
			Map<String, String> fields, Map<String, Object> details) {
		this.status = status;
		this.code = code;
		this.error = error;
		this.message = message;
		this.path = path;
		this.requestId = requestId;
		this.fields = fields;
		this.details = details;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}
}
