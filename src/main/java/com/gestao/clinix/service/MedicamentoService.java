package com.gestao.clinix.service;

import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.gestao.clinix.dto.MedicamentoPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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

    public MedicamentoPageResponse getAllMedicamentosByNomeComercial(String nomeComercial, Integer pageSize, Integer pageNumber, String sortBy, String sortOrder) {
        Sort sortByNomeComercial = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByNomeComercial);

        Specification<Medicamento> spec = Specification.allOf(List.of());

        if(nomeComercial!=null && !nomeComercial.isEmpty()){
            spec = spec.and((root, query, criteriaBuilder)
                    -> criteriaBuilder.like(criteriaBuilder.lower(root.get("nomeComercial")), "%"+nomeComercial.toLowerCase()+"%"));
        }

        Page<Medicamento> medicamentosPage = medicationRepository.findAll(spec, pageable);

        return new MedicamentoPageResponse(
                medicamentosPage.getContent().stream().map(medicationMapper::toResponse).toList(),
                medicamentosPage.getSize(), medicamentosPage.getNumber(),
                medicamentosPage.getTotalElements(), medicamentosPage.getTotalPages(),
                medicamentosPage.isLast(), LocalDateTime.now(ZoneOffset.UTC));
    }

}
