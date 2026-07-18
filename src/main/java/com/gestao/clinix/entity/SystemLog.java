package com.gestao.clinix.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_logs")
public class SystemLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "action", nullable = false, length = 50)
	private String action;

	@Column(name = "entity_name", nullable = false, length = 100)
	private String entityName;

	@Column(name = "entity_id")
	private Long entityId;

	@Column(name = "performed_by", nullable = false, length = 255)
	private String performedBy;

	@Column(name = "performed_at", nullable = false)
	private LocalDateTime performedAt;

	@Column(name = "details", length = 1000)
	private String details;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public String getPerformedBy() {
		return performedBy;
	}

	public void setPerformedBy(String performedBy) {
		this.performedBy = performedBy;
	}

	public LocalDateTime getPerformedAt() {
		return performedAt;
	}

	public void setPerformedAt(LocalDateTime performedAt) {
		this.performedAt = performedAt;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
}
