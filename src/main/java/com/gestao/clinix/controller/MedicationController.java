package com.gestao.clinix.controller;

import java.net.URI;
import com.gestao.clinix.dto.MedicationRequest;
import com.gestao.clinix.dto.MedicationResponse;
import com.gestao.clinix.service.MedicationOptions;
import com.gestao.clinix.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medications")
public class MedicationController {
    private final MedicationService service;
    private final MedicationOptions options;

    public MedicationController(MedicationService service, MedicationOptions options) {
        this.service = service;
        this.options = options;
    }

    @GetMapping
    public MedicationService.MedicationPage list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "medicationName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return service.list(search, pageNumber, pageSize, sortBy, sortOrder);
    }

    @GetMapping("/options")
    public MedicationOptions.Options options() { return options.getAll(); }

    @GetMapping("/{id}")
    public MedicationResponse getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicationResponse> create(@Valid @RequestBody MedicationRequest request) {
        MedicationResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/medications/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MedicationResponse update(@PathVariable Long id, @Valid @RequestBody MedicationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
