package com.gestao.clinix.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gestao.clinix.dto.MedicamentoDTO;
import com.gestao.clinix.entity.Medicamento;
import com.gestao.clinix.repository.MedicamentoRepository;
import com.gestao.clinix.mapper.MedicamentoMapper;

import jakarta.persistence.EntityNotFoundException;



@Service
public class MedicamentoService {
	
	private final MedicamentoRepository medicamentoRepository;
	private final MedicamentoMapper medicamentoMapper;

	public MedicamentoService(MedicamentoRepository medicamentoRepository, MedicamentoMapper medicamentoMapper) {
		this.medicamentoRepository = medicamentoRepository;
		this.medicamentoMapper = medicamentoMapper;
	}
	
	public List<MedicamentoDTO> getAll(){
		return medicamentoRepository.findAll().stream()
				.map(medicamentoMapper::toResponse)
				.toList();
	}
	
	public MedicamentoDTO getById(Long id) {
		Medicamento medicamento = medicamentoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Não existe medicamento como ID: " + id));
		return medicamentoMapper.toResponse(medicamento);
	}
	
	public void post(MedicamentoDTO medicamentoDTO) {
		Medicamento medicamento = medicamentoMapper.toEntity(medicamentoDTO);
		medicamentoRepository.save(medicamento);
	}
	
	public void update(MedicamentoDTO medicamentoDTO) {
		Medicamento medicamento = medicamentoRepository.findById(medicamentoDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Não existe um medicamento com esse ID: " + medicamentoDTO.getId()));
		medicamentoMapper.updateEntity(medicamentoDTO, medicamento);
		
		medicamentoRepository.save(medicamento);
	}
	
	public void delete(Long id) {
		medicamentoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Não existe um medicamento com ID: " + id));
		medicamentoRepository.deleteById(id);
	}
	


}
