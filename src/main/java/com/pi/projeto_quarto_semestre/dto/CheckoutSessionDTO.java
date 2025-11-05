package com.pi.projeto_quarto_semestre.dto;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutSessionDTO {

    public static class Item {
        private Long produtoId;
        private String nome;
        private BigDecimal precoUnit;
        private Integer quantidade;

        public BigDecimal getSubtotal() {
            if (precoUnit == null || quantidade == null) return BigDecimal.ZERO;
            return precoUnit.multiply(BigDecimal.valueOf(quantidade));
        }
        public Long getProdutoId() { return produtoId; }
        public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public BigDecimal getPrecoUnit() { return precoUnit; }
        public void setPrecoUnit(BigDecimal precoUnit) { this.precoUnit = precoUnit; }
        public Integer getQuantidade() { return quantidade; }
        public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    }

    public static class Pagamento {
        private String metodo; // "BOLETO" ou "CARTAO"
        private String nomeCompleto;
        private String numeroCartao; // mascarado
        private String validade;
        private String parcelas;
        public String getMetodo() { return metodo; }
        public void setMetodo(String metodo) { this.metodo = metodo; }
        public String getNomeCompleto() { return nomeCompleto; }
        public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
        public String getNumeroCartao() { return numeroCartao; }
        public void setNumeroCartao(String numeroCartao) { this.numeroCartao = numeroCartao; }
        public String getValidade() { return validade; }
        public void setValidade(String validade) { this.validade = validade; }
        public String getParcelas() { return parcelas; }
        public void setParcelas(String parcelas) { this.parcelas = parcelas; }
    }

    private List<Item> itens;
    private BigDecimal frete;

    public List<Item> getItens() { return itens; }
    public void setItens(List<Item> itens) { this.itens = itens; }
    public BigDecimal getFrete() { return frete; }
    public void setFrete(BigDecimal frete) { this.frete = frete; }
}
