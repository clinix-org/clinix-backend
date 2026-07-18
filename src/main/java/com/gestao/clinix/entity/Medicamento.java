package com.gestao.clinix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Medicamento")
public class Medicamento {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="NOME_COMERCIAL")
	private String nomeComercial;
	
	@Column(name="NOME_GENERICO")
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
	
	
	
	/*@Column(name="CODIGO")
	private Long codigo;
	
	@Column(name="FORMA_FARMACEUTICA")
	private String formaFarmaceutica;
	
	@Column(name="VIA_ADMINISTRACAO")
	private String viaAdministacao;
	
	@Column(name="CONCENTRACAO")
	private String concentracao;
	
	@Column(name="Quantidade")
	private int quantidade;
	
	@Column(name="UNIDADE_MEDIDA")
	private String unidadeMedida;
	
	@Column(name="REGISTRO_ANVISA")
	private Long registroAnvisa;
	
	@Column(name="CLASSE_TERAPEUTICA")
	private String classeTerapeutica;
	
	@Column(name="TARJA")
	private String tarja;
	
	@Column(name="FABRICANTE")
	private String fabricante;
	
	@Column(name="DATA_VALIDADE")
	private Date dataValidade;
	
	@Column(name="LOTE")
	private String lote;
	
	@Column(name="TIPO_CONTROLE_ESPECIAL")
	private String tipoControleEspecial;
	
	@Column(name="PRECO_CUSTO")
	private double precoCusto;
	
	@Column(name="FORNECEDOR")
	private String fornecedor;
	
	@Column(name="ESTOQUE_MINIMO")
	private int estoqueMinimo;
	
	@Column(name="ESTOQUE_MAXIMO")
	private int estoqueMaximo;
	
	@Column(name="ESTOQUE_ATUAL")
	private int estoqueAtual;
	
	@Column(name="LOCALIZACAO_ESTOQUE")
	private String localizacaoEstoque;*/	

}
