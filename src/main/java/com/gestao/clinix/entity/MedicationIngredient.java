package com.gestao.clinix.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Embeddable
public class MedicationIngredient {
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    @Column(name = "dosage", nullable = false, precision = 18, scale = 6)
    private BigDecimal dosage;
    @Column(name = "unit_measurement", nullable = false, length = 20)
    private String unitMeasurement;

    protected MedicationIngredient() {}

    public MedicationIngredient(String name, BigDecimal dosage, String unitMeasurement) {
        this.name = name;
        this.dosage = dosage;
        this.unitMeasurement = unitMeasurement;
    }

    public String getName() { return name; }
    public BigDecimal getDosage() { return dosage; }
    public String getUnitMeasurement() { return unitMeasurement; }
}
