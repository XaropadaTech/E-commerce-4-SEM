package com.pi.projeto_quarto_semestre.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PagamentoCartao {
    @NotBlank(message = "Número do cartão é obrigatório")
    @Size(min = 13, max = 19, message = "Número do cartão inválido")
    private String numeroCartao;

    @NotBlank(message = "CVV é obrigatório")
    @Size(min = 3, max = 4, message = "CVV inválido")
    private String cvv;

    @NotBlank(message = "Nome do titular é obrigatório")
    private String nomeTitular;

    @NotBlank(message = "Data de vencimento é obrigatória")
    private String dataVencimento; // MM/yy ou MM/yyyy — validar conforme necessidade

    @NotNull(message = "Quantidade de parcelas é obrigatória")
    private Integer numeroParcelas;

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getNomeTitular() {
        return nomeTitular;
    }

    public void setNomeTitular(String nomeTitular) {
        this.nomeTitular = nomeTitular;
    }

    public String getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(String dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Integer getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(Integer numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }
    
}
