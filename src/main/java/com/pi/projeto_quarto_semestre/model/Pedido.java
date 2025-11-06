package com.pi.projeto_quarto_semestre.model;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {
    // Requisito 2: "Um número sequencial do pedido deve ser gerado"
    // O @GeneratedValue(strategy = GenerationType.IDENTITY) faz isso automaticamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Este será o "número sequencial"

    // Precisamos saber QUEM fez o pedido
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Precisamos saber PARA ONDE entregar o pedido
    @ManyToOne(optional = false)
    @JoinColumn(name = "endereco_entrega_id")
    private Endereco enderecoEntrega;

    // Requisito 1: "status de 'aguardando pagamento'"
    @Column(nullable = false, length = 50)
    private String status; // Ex: "AGUARDANDO_PAGAMENTO", "PAGO", "ENVIADO"

    // Requisito (do checkout): "forma de pagamento"
    @Column(nullable = false, length = 50)
    private String formaPagamento; // Ex: "BOLETO", "CARTAO_CREDITO"

    // Requisito 3: "valor"
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal; // A soma de todos os itens + frete

    // Requisito (implícito): "data"
    @Column(nullable = false)
    private LocalDateTime dataPedido;

    // Um Pedido tem vários Itens.
    // cascade = CascadeType.ALL: Se o pedido for apagado, os itens vão junto.
    // orphanRemoval = true: Se um item for removido da lista, ele é apagado do banco.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    // Construtor padrão (necessário para JPA)
    public Pedido() {
    }

    // Método "helper" para facilitar a adição de itens
    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
    }

    // --- Getters e Setters ---
    // (Você pode gerar todos na sua IDE, ou copiar e colar abaixo)

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

    public Endereco getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(Endereco enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

}
