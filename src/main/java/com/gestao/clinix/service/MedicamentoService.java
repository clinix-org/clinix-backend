package com.gestao.clinix.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.gestao.clinix.dto.MedicamentoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    public MedicamentoResponse getAllMedicamentosByNomeComercial(String nomeComercial, Integer pageSize, Integer pageNumber, String sortBy, String sortOrder) {
        Sort sortByNomeComercial = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByNomeComercial);

        Specification<Medicamento> spec = Specification.allOf(List.of());

        if(nomeComercial!=null && !nomeComercial.isEmpty()){
            spec = spec.and((root, query, criteriaBuilder)
                    -> criteriaBuilder.like(criteriaBuilder.lower(root.get("nomeComercial")), "%"+nomeComercial.toLowerCase()+"%"));
        }

        Page<Medicamento> medicamentosPage = medicamentoRepository.findAll(spec, pageable);

        List<Medicamento> medicamentos = medicamentosPage.getContent();

        List<MedicamentoDTO> medicamentosDTOs = medicamentos.stream()
                .map(medicamento -> {return medicamentoMapper.toResponse(medicamento);}).toList();

        MedicamentoResponse medicamentoResponse = new MedicamentoResponse();
        medicamentoResponse.setMedicamentos(medicamentosDTOs);
        medicamentoResponse.setPageNumber(medicamentosPage.getNumber());
        medicamentoResponse.setPageSize(medicamentosPage.getSize());
        medicamentoResponse.setTotalPages(medicamentosPage.getTotalPages());
        medicamentoResponse.setTotalElements(medicamentosPage.getTotalElements());
        medicamentoResponse.setLastPage(medicamentosPage.isLast());
        medicamentoResponse.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));

        return medicamentoResponse;
    }

}
