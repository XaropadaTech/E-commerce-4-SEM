package com.pi.projeto_quarto_semestre.model;

import jakarta.persistence.*;

@Entity
@Table(name = "endereco")
public class Endereco {

    public enum Tipo { FATURAMENTO, ENTREGA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=15)
    private Tipo tipo;

    @Column(nullable=false, length=8)
    private String cep; // só dígitos (ex: 09760280)

    @Column(nullable=false, length=120)
    private String logradouro;

    @Column(nullable=false, length=10)
    private String numero;

    @Column(length=60)
    private String complemento;

    @Column(nullable=false, length=60)
    private String bairro;

    @Column(nullable=false, length=60)
    private String cidade;

    @Column(nullable=false, length=2)
    private String uf;

    @Column(nullable = false) // Garante que o campo sempre terá um valor (não será nulo)
    private Boolean padrao = false; // Define 'false' como valor inicial padrão

        // getters e setters 


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public Boolean getPadrao() {
        return padrao;
    }

    public void setPadrao(Boolean padrao) {
        this.padrao = padrao;
    }
    
}
