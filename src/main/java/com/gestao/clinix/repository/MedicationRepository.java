package com.gestao.clinix.repository;

import com.gestao.clinix.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MedicationRepository extends JpaRepository<Medication, Long>,
        JpaSpecificationExecutor<Medication> {
    boolean existsByDuplicateKeyAndIdNot(String duplicateKey, Long id);
}
