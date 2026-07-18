package com.gestao.clinix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestao.clinix.entity.SystemLog;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
}
