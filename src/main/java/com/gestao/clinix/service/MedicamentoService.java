package com.gestao.clinix.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gestao.clinix.dto.MedicamentoDTO;
import com.gestao.clinix.dto.MedicamentoResponse;
import com.gestao.clinix.entity.Medicamento;
import com.gestao.clinix.exception.ResourceNotFoundException;
import com.gestao.clinix.mapper.MedicamentoMapper;
import com.gestao.clinix.repository.MedicamentoRepository;

@Service
public class MedicamentoService {

	private final MedicamentoRepository medicationRepository;
	private final MedicamentoMapper medicationMapper;

	public MedicamentoService(MedicamentoRepository medicationRepository, MedicamentoMapper medicationMapper) {
		this.medicationRepository = medicationRepository;
		this.medicationMapper = medicationMapper;
	}

	public List<MedicamentoResponse> getAll(){
		return medicationRepository.findAll().stream()
				.map(medicationMapper::toResponse)
				.toList();
	}

	public MedicamentoResponse getById(Long id) {
		Medicamento medication = medicationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Não existe medicamento como ID: " + id));
		return medicationMapper.toResponse(medication);
	}

	public void post(MedicamentoDTO medicationRequest) {
		Medicamento medication = medicationMapper.toEntity(medicationRequest);
		medicationRepository.save(medication);
	}

	public void update(MedicamentoDTO medicationRequest) {
		Medicamento medication = medicationRepository.findById(medicationRequest.getId()).orElseThrow(() -> new ResourceNotFoundException("Não existe um medicamento com esse ID: " + medicationRequest.getId()));
		medicationMapper.updateEntity(medicationRequest, medication);

		medicationRepository.save(medication);
	}

	public void delete(Long id) {
		medicationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Não existe um medicamento com ID: " + id));
		medicationRepository.deleteById(id);
	}

}
