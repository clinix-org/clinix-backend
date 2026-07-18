package com.gestao.clinix.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.gestao.clinix.service.MedicamentoService;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/medicamentos")
public class MedicamentoController {
	
	@Autowired
	private MedicamentoService medicamentoService;
	
	
	@GetMapping()
	public ResponseEntity<?> getAll() {
		try {
			List<MedicamentoDTO> medicamentosDTO = getMedicamentoService().getAll();
			return ResponseEntity.status(HttpStatus.OK).body(medicamentosDTO);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}		
	}	
	
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getById(@PathVariable Long id) {
		try {
			MedicamentoDTO livroDTO = getMedicamentoService().getById(id);
			return new ResponseEntity<>(livroDTO, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Medicamento não encontrado!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping()
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> post(@RequestBody MedicamentoDTO medicamentoDTO){
		try {
			getMedicamentoService().post(medicamentoDTO);
			return new ResponseEntity<>("Medicamento cadastrado com sucesso!", HttpStatus.OK);			
		} catch (Exception e ) {
			return new ResponseEntity<>("Falha ao cadastrar o medicamento", HttpStatus.INTERNAL_SERVER_ERROR);			
		}
	}
	
	@PutMapping()
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> update(@RequestBody MedicamentoDTO medicamentoDTO) {
		try {
			getMedicamentoService().update(medicamentoDTO);
			return new ResponseEntity<>("Medicamento alterado com sucesso!", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Falha ao atualizar o medicamento!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@DeleteMapping() 
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> delete (@RequestParam Long id) {
		try {
			getMedicamentoService().delete(id);
			return new ResponseEntity<>("Medicamento deletado com sucesso!", HttpStatus.OK);		
		} catch (Exception e) {
			return new ResponseEntity<>("Falha ao tentar excluir o medicamento!", HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}	
	
	
	public MedicamentoService getMedicamentoService() {
		return medicamentoService;
	}

}
