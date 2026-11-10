package com.gestao.clinix.dto;

public class MedicamentoDTO {
	
	
	private Long id;
	
	
	private String nomeComercial;
	
	
	private String nomeGenerico;

    private int quantidade;

    private String categoria;

    private String unidadeMedida;

    private String status;
	
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

    public int getQuantidade() {return quantidade;}

    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public String getCategoria() {return categoria;}

    public void setCategoria(String categoria) {this.categoria = categoria;}

    public String getUnidadeMedida() {return unidadeMedida;}

    public void setUnidadeMedida(String unidadeMedida) {this.unidadeMedida = unidadeMedida;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}
}
