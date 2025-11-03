package com.pi.projeto_quarto_semestre.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class Pagamento {

    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;

    @Valid
    private PagamentoCartao pagamentoCartao;

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public PagamentoCartao getPagamentoCartao() {
        return pagamentoCartao;
    }

    public void setPagamentoCartao(PagamentoCartao pagamentoCartao) {
        this.pagamentoCartao = pagamentoCartao;
    }
    
}
