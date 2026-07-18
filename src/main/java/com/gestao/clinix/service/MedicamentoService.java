package com.gestao.clinix.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestao.clinix.dto.MedicamentoDTO;
import com.gestao.clinix.entity.Medicamento;
import com.gestao.clinix.repository.MedicamentoRepository;
import com.gestao.clinix.utils.MedicamentoMapper;

import jakarta.persistence.EntityNotFoundException;



@Service
public class MedicamentoService {
	
	@Autowired
	private MedicamentoRepository medicamentoRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private MedicamentoMapper medicamentoMapper;
	
	public List<MedicamentoDTO> getAll(){
		List<Medicamento> medicamentoList = getMedicamentoRepository().findAll();
		List<MedicamentoDTO> livroListDTO = medicamentoList.stream().map(medicamento -> MedicamentoDTO.convertToDTO(medicamento)).toList();
		return livroListDTO;
	}
	
	public MedicamentoDTO getById(Long id) {
		Medicamento medicamento = getMedicamentoRepository().findById(id).orElseThrow(() -> new EntityNotFoundException("Não existe medicamento como ID: " + id));
		return modelMapper.map(medicamento, MedicamentoDTO.class);
	}
	
	public void post(MedicamentoDTO medicamentoDTO) {
		Medicamento medicamento = getMedicamentoMapper().convertToEntity(medicamentoDTO);
		getLivroRepository().save(medicamento);
	}
	
	public void update(MedicamentoDTO medicamentoDTO) {
		Medicamento medicamento = getLivroRepository().findById(medicamentoDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Não existe um medicamento com esse ID: " + medicamentoDTO.getId()));
		medicamento.setId(medicamentoDTO.getId());
		medicamento.setNomeComercial(medicamentoDTO.getNomeComercial());
		medicamento.setNomeGenerico(medicamentoDTO.getNomeGenerico());		
		
		getLivroRepository().save(medicamento);		
	}
	
	public void delete(Long id) {
		getLivroRepository().findById(id).orElseThrow(() -> new EntityNotFoundException("Não existe um livro com ID: " + id));
		getLivroRepository().deleteById(id);
	}
	
	public MedicamentoRepository getLivroRepository() {
		return medicamentoRepository;
	}
	
	public MedicamentoMapper getMedicamentoMapper() {
		return medicamentoMapper;
	}
	
	public ModelMapper getModelMapper() {
		return modelMapper;
	}	
	
	public MedicamentoRepository getMedicamentoRepository() {
		return medicamentoRepository;
	}


}
