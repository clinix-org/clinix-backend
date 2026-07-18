package com.gestao.clinix.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

	public static final String REQUEST_ID_ATTRIBUTE = "requestId";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String requestId = request.getHeader(REQUEST_ID_HEADER);
		if (requestId == null || requestId.isBlank()) {
			requestId = UUID.randomUUID().toString();
		}

		request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
		response.setHeader(REQUEST_ID_HEADER, requestId);
		MDC.put(REQUEST_ID_ATTRIBUTE, requestId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(REQUEST_ID_ATTRIBUTE);
		}
	}
}
