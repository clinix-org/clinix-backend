package com.gestao.clinix.dto;

public class MedicamentoDTO {
	
	
	private Long id;
	
	
	private String nomeComercial;
	
	
	private String nomeGenerico;	
	
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}
		

	public String getNomeComercial() {
		return nomeComercial;
	}


	public void setNomeComercial(String nomeComercial) {
		this.nomeComercial = nomeComercial;
	}


	public String getNomeGenerico() {
		return nomeGenerico;
	}


	public void setNomeGenerico(String nomeGenerico) {
		this.nomeGenerico = nomeGenerico;
	}	
	
}
