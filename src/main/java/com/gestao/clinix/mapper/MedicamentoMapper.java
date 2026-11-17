package com.gestao.clinix.mapper;

import org.springframework.stereotype.Component;

import com.gestao.clinix.dto.MedicamentoDTO;
import com.gestao.clinix.dto.MedicamentoResponse;
import com.gestao.clinix.entity.Medicamento;

@Component
public class MedicamentoMapper {

	public MedicamentoResponse toResponse(Medicamento medicamento) {
		return new MedicamentoResponse(medicamento.getId(), medicamento.getNomeComercial(),
				medicamento.getNomeGenerico(), medicamento.getQuantidade(), medicamento.getUnidadeMedida(),
				medicamento.getCategoria(), medicamento.getStatus());
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
		medicamento.setQuantidade(dto.getQuantidade());
		medicamento.setUnidadeMedida(dto.getUnidadeMedida());
		medicamento.setCategoria(dto.getCategoria());
		medicamento.setStatus(dto.getStatus());
	}
}
