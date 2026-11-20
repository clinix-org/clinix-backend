package com.gestao.clinix.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import com.gestao.clinix.dto.MedicationRequest;
import com.gestao.clinix.dto.MedicationResponse;
import com.gestao.clinix.entity.*;
import com.gestao.clinix.exception.DuplicateResourceException;
import com.gestao.clinix.exception.ResourceNotFoundException;
import com.gestao.clinix.repository.MedicationRepository;
import com.gestao.clinix.security.AuthenticatedUserProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicationService {
    public static final String DUPLICATE_MESSAGE = "Medicamento já cadastrado.";
    private final MedicationRepository repository;
    private final MedicationOptions options;
    private final AuthenticatedUserProvider authenticatedUser;
    private final SystemLogService logs;

    public MedicationService(MedicationRepository repository, MedicationOptions options,
            AuthenticatedUserProvider authenticatedUser, SystemLogService logs) {
        this.repository = repository;
        this.options = options;
        this.authenticatedUser = authenticatedUser;
        this.logs = logs;
    }

    @Transactional
    public MedicationResponse create(MedicationRequest request) {
        Medication medication = new Medication();
        medication.setQuantity(0);
        medication.setStatus("UNAVAILABLE");
        medication.setCreatedAt(Instant.now());
        medication.setCreatedBy(authenticatedUser.getAuthenticatedUsername());
        apply(request, medication);
        persist(medication);
        audit(SystemLogAction.CREATE, medication);
        return toResponse(medication);
    }

    @Transactional
    public MedicationResponse update(Long id, MedicationRequest request) {
        Medication medication = find(id);
        apply(request, medication);
        persist(medication);
        audit(SystemLogAction.UPDATE, medication);
        return toResponse(medication);
    }

    @Transactional
    public void delete(Long id) {
        Medication medication = find(id);
        repository.delete(medication);
        audit(SystemLogAction.DELETE, medication);
    }

    @Transactional(readOnly = true)
    public MedicationResponse getById(Long id) { return toResponse(find(id)); }

    public record MedicationPage(List<MedicationResponse> content, int pageNumber,
            int pageSize, long totalElements, int totalPages, boolean lastPage) {}

    @Transactional(readOnly = true)
    public MedicationPage list(String search, int pageNumber, int pageSize,
            String sortBy, String sortOrder) {
        if (pageNumber < 0 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("Paginação inválida. Use páginas de 1 a 100 itens.");
        }
        if (!Set.of("medicationName", "id", "category", "quantity", "status", "createdAt")
                .contains(sortBy) || !("asc".equalsIgnoreCase(sortOrder)
                        || "desc".equalsIgnoreCase(sortOrder))) {
            throw new IllegalArgumentException("Ordenação inválida.");
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        if (!"id".equals(sortBy)) sort = sort.and(Sort.by("id"));
        Specification<Medication> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String term = normalize(search).replace("\\", "\\\\")
                    .replace("%", "\\%").replace("_", "\\_");
            return cb.like(cb.lower(root.get("medicationName")), "%" + term + "%", '\\');
        };
        Page<Medication> page = repository.findAll(spec, PageRequest.of(pageNumber, pageSize, sort));
        return new MedicationPage(page.getContent().stream().map(this::toResponse).toList(),
                pageNumber, pageSize, page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    private Medication find(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Medicamento não encontrado."));
    }

    private void apply(MedicationRequest request, Medication medication) {
        options.validate(request.category(), options.getAll().categories(), "categoria");
        options.validate(request.presentationFormat(), options.getAll().presentationFormats(),
                "forma de apresentação");
        Set<String> ingredientNames = new HashSet<>();
        List<String> composition = new ArrayList<>();
        List<MedicationIngredient> ingredients = new ArrayList<>();
        for (MedicationRequest.Ingredient ingredient : request.activeIngredient()) {
            options.validate(ingredient.unitMeasurement(), options.getAll().units(), "unidade de medida");
            String name = normalize(ingredient.name());
            if (!ingredientNames.add(name)) {
                throw new IllegalArgumentException("Informe cada princípio ativo apenas uma vez.");
            }
            // Length prefixes make the key unambiguous even when names contain punctuation.
            composition.add(part(name) + part(ingredient.dosage().stripTrailingZeros().toPlainString())
                    + part(ingredient.unitMeasurement()));
            ingredients.add(new MedicationIngredient(clean(ingredient.name()), ingredient.dosage(),
                    ingredient.unitMeasurement()));
        }
        Collections.sort(composition);
        String duplicateKey = hash(part(normalize(request.medicationName()))
                + String.join("", composition));
        if (repository.existsByDuplicateKeyAndIdNot(duplicateKey,
                medication.getId() == null ? -1L : medication.getId())) {
            throw new DuplicateResourceException(DUPLICATE_MESSAGE);
        }
        medication.setMedicationName(clean(request.medicationName()));
        medication.setCategory(request.category());
        medication.setPresentationFormat(request.presentationFormat());
        medication.setManufacturer(clean(request.manufacturer()));
        medication.setDuplicateKey(duplicateKey);
        medication.getActiveIngredient().clear();
        medication.getActiveIngredient().addAll(ingredients);
    }

    private void persist(Medication medication) {
        try {
            repository.saveAndFlush(medication);
        } catch (DataIntegrityViolationException exception) {
            // The unique constraint also catches two simultaneous registrations.
            if (exception.getMostSpecificCause().getMessage() != null
                    && exception.getMostSpecificCause().getMessage().toLowerCase(Locale.ROOT)
                            .contains("uk_medications_duplicate_key")) {
                throw new DuplicateResourceException(DUPLICATE_MESSAGE);
            }
            throw exception;
        }
    }

    private void audit(SystemLogAction action, Medication medication) {
        logs.register(action, "Medication", medication.getId(),
                authenticatedUser.getAuthenticatedUsername(),
                LocalDateTime.now(ZoneOffset.UTC), "Medication " + action.name().toLowerCase(Locale.ROOT));
    }

    private MedicationResponse toResponse(Medication medication) {
        return new MedicationResponse(medication.getId(), medication.getMedicationName(),
                medication.getActiveIngredient().stream().map(ingredient ->
                        new MedicationRequest.Ingredient(ingredient.getName(), ingredient.getDosage(),
                                ingredient.getUnitMeasurement())).toList(),
                medication.getCategory(), medication.getPresentationFormat(), medication.getManufacturer(),
                medication.getQuantity(), medication.getStatus(), medication.getCreatedAt(), medication.getCreatedBy());
    }

    private static String clean(String value) { return value.replaceAll("(?U)\\s+", " ").strip(); }
    private static String normalize(String value) { return clean(value).toLowerCase(Locale.ROOT); }
    private static String part(String value) { return value.length() + ":" + value; }
    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }
}
