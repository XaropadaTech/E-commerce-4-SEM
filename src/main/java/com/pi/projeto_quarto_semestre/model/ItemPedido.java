package com.pi.projeto_quarto_semestre.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
public class ItemPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A qual pedido este item pertence?
    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    // Qual produto é este?
    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto; // Referencia a classe Produto que você já tem

    // Quantas unidades deste produto?
    @Column(nullable = false)
    private Integer quantidade;

    // Qual era o preço do produto NO MOMENTO da compra?
    // (Importante guardar, pois o preço do produto na loja pode mudar no futuro)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    // Construtor padrão
    public ItemPedido() {
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
}
