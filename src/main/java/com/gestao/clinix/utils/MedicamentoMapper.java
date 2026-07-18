package com.gestao.clinix.utils;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gestao.clinix.dto.MedicamentoDTO;
import com.gestao.clinix.entity.Medicamento;

@Component
public class MedicamentoMapper {
	
	@Autowired
	private ModelMapper modelMapper;
	
	public Medicamento convertToEntity(MedicamentoDTO medicamentoDTO) {
		Medicamento medicamento = new Medicamento();
		medicamento.setId(medicamentoDTO.getId());
		medicamento.setNomeComercial(medicamentoDTO.getNomeComercial());
		medicamento.setNomeGenerico(medicamentoDTO.getNomeGenerico());		
		return medicamento;
	}

	
	/*public Medicamento convertToEntity(MedicamentoDTO medicamentoDTO) {
		return getModelMapper().map(medicamentoDTO, Medicamento.class);
		
	}*/
	
	public ModelMapper getModelMapper() {
		return modelMapper;
	}

}
