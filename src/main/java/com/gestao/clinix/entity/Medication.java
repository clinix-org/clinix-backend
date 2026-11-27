package com.gestao.clinix.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "medications", uniqueConstraints = @UniqueConstraint(
        name = "uk_medications_duplicate_key", columnNames = "duplicate_key"))
public class Medication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "medication_name", nullable = false, length = 200)
    private String medicationName;
    @Column(nullable = false, length = 50)
    private String category;
    @Column(name = "presentation_format", nullable = false, length = 50)
    private String presentationFormat;
    @Column(nullable = false, length = 200)
    private String manufacturer;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    @Column(name = "duplicate_key", nullable = false, length = 64)
    private String duplicateKey;

    @ElementCollection
    @org.hibernate.annotations.BatchSize(size = 100)
    @CollectionTable(name = "medication_active_ingredients",
            joinColumns = @JoinColumn(name = "medication_id"))
    @OrderColumn(name = "ingredient_order")
    private List<MedicationIngredient> activeIngredient = new ArrayList<>();

    public List<MedicationIngredient> getActiveIngredient() { return activeIngredient; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPresentationFormat() { return presentationFormat; }
    public void setPresentationFormat(String presentationFormat) { this.presentationFormat = presentationFormat; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getDuplicateKey() { return duplicateKey; }
    public void setDuplicateKey(String duplicateKey) { this.duplicateKey = duplicateKey; }
}
