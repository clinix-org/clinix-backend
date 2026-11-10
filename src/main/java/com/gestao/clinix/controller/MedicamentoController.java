package com.gestao.clinix.controller;

import java.util.List;

import com.gestao.clinix.config.AppConsts;
import com.gestao.clinix.dto.MedicamentoPageResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestao.clinix.dto.MedicamentoDTO;
import com.gestao.clinix.dto.MedicamentoResponse;
import com.gestao.clinix.service.MedicamentoService;

@RestController
@RequestMapping("/medicamentos")
public class MedicamentoController {

	private final MedicamentoService medicationService;

	public MedicamentoController(MedicamentoService medicationService) {
		this.medicationService = medicationService;
	}

	@GetMapping()
	public ResponseEntity<List<MedicamentoResponse>> getAll() {
		return ResponseEntity.ok(medicationService.getAll());
	}

	@GetMapping("/nomecomercial")
	public ResponseEntity<MedicamentoPageResponse> getAllMedicamentosByNomeComercial(
			@RequestParam(name = "nomeComercial", required = false) String nomeComercial,
			@RequestParam(name = "pageSize", defaultValue = AppConsts.PAGE_SIZE) Integer pageSize,
			@RequestParam(name = "pageNumber", defaultValue = AppConsts.PAGE_NUM) Integer pageNumber,
			@RequestParam(name = "sortBy", defaultValue = AppConsts.SORT_MEDICAMENTOS_BY) String sortBy,
			@RequestParam(name = "sortOrder", defaultValue = AppConsts.SORT_DIRECTION) String sortOrder) {
		return ResponseEntity.ok(medicationService.getAllMedicamentosByNomeComercial(
				nomeComercial, pageSize, pageNumber, sortBy, sortOrder));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MedicamentoResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(medicationService.getById(id));
	}

	@PostMapping()
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> post(@RequestBody MedicamentoDTO medicationRequest) {
		medicationService.post(medicationRequest);
		return ResponseEntity.ok("Medicamento cadastrado com sucesso!");
	}

	@PutMapping()
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> update(@RequestBody MedicamentoDTO medicationRequest) {
		medicationService.update(medicationRequest);
		return ResponseEntity.ok("Medicamento alterado com sucesso!");
	}

	@DeleteMapping()
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> delete(@RequestParam Long id) {
		medicationService.delete(id);
		return ResponseEntity.ok("Medicamento deletado com sucesso!");
	}

}
