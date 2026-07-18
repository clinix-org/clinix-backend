package com.gestao.clinix.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.gestao.clinix.entity.SystemLogAction;
import com.gestao.clinix.entity.SystemLog;
import com.gestao.clinix.repository.SystemLogRepository;

@Service
public class SystemLogService {

	private final SystemLogRepository repository;

	public SystemLogService(SystemLogRepository repository) {
		this.repository = repository;
	}

	public void register(SystemLogAction action, String entityName, Long entityId, String performedBy,
			LocalDateTime performedAt, String details) {
		SystemLog log = new SystemLog();
		log.setAction(action.name());
		log.setEntityName(entityName);
		log.setEntityId(entityId);
		log.setPerformedBy(performedBy);
		log.setPerformedAt(performedAt);
		log.setDetails(sanitizeDetails(details));

		repository.save(log);
	}

	private String sanitizeDetails(String details) {
		if (details == null) {
			return null;
		}

		String sanitized = details.replace('\r', ' ').replace('\n', ' ').trim();
		return sanitized.length() <= 1000 ? sanitized : sanitized.substring(0, 1000);
	}
}
