package com.pi.projeto_quarto_semestre.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(
    name = "cliente",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_cliente_cpf", columnNames = "cpf")
    }
)
public class Cliente {

    public enum Genero { MASCULINO, FEMININO, OUTRO, PREFIRO_NAO_INFORMAR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String nomeCompleto;

    @Column(nullable=false, length=120)
    private String email;

    @Column(nullable=false, length=14)
    private String cpf; // armazenar só dígitos é melhor (11). Se preferir, normalize.

    @Column(nullable=false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private Genero genero;

    @Column(nullable=false, length=80)
    private String senhaHash; // usar PasswordEncoder existente

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();

    public void addEndereco(Endereco e) {
        e.setCliente(this);
        this.enderecos.add(e);
    }

     // getters e setters 


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public List<Endereco> getEnderecos() {
        return enderecos;
    }

    public void setEnderecos(List<Endereco> enderecos) {
        this.enderecos = enderecos;
    }

    
}
