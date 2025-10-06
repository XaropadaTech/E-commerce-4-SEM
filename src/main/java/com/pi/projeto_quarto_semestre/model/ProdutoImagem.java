package com.pi.projeto_quarto_semestre.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "produto_imagem")
public class ProdutoImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "caminho_imagem", length = 255, nullable = false)
    private String caminhoImagem;

    @Column(name = "nome_original")
    private String nomeOriginal;

    @Column(name = "imagem_padrao", nullable = false)
    private Boolean imagemPadrao = false;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name="caminho_relativo", length=500)
    private String caminhoRelativo; // ex.: products/42/42_abc123.jpg


    // Getters e Setters

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getCaminhoImagem() {
        return caminhoImagem;
    }

    public void setCaminhoImagem(String caminhoImagem) {
        this.caminhoImagem = caminhoImagem;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public Boolean getImagemPadrao() {
        return imagemPadrao;
    }

    public void setImagemPadrao(Boolean imagemPadrao) {
        this.imagemPadrao = imagemPadrao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public String getCaminhoRelativo() {
        return caminhoRelativo;
    }

    public void setCaminhoRelativo(String caminhoRelativo) {
        this.caminhoRelativo = caminhoRelativo;
    }
}