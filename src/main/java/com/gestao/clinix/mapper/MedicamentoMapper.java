package com.gestao.clinix.mapper;

import org.springframework.stereotype.Component;

import com.gestao.clinix.dto.MedicamentoDTO;
import com.gestao.clinix.entity.Medicamento;

@Component
public class MedicamentoMapper {

	public MedicamentoDTO toResponse(Medicamento medicamento) {
		MedicamentoDTO dto = new MedicamentoDTO();
		dto.setId(medicamento.getId());
		dto.setNomeComercial(medicamento.getNomeComercial());
		dto.setNomeGenerico(medicamento.getNomeGenerico());
		return dto;
	}

	public Medicamento toEntity(MedicamentoDTO dto) {
		Medicamento medicamento = new Medicamento();
		medicamento.setId(dto.getId());
		updateEntity(dto, medicamento);
		return medicamento;
	}

	public void updateEntity(MedicamentoDTO dto, Medicamento medicamento) {
		medicamento.setNomeComercial(dto.getNomeComercial());
		medicamento.setNomeGenerico(dto.getNomeGenerico());
	}
}
