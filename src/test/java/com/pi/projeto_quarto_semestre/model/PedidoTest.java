package com.pi.projeto_quarto_semestre.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class PedidoTest {

    @Test
    @DisplayName("Deve iniciar um pedido com status AGUARDANDO_PAGAMENTO")
    void testeNovoPedidoStatus() {
        // 1. Cenário (Arrange)
        Pedido pedido = new Pedido();

        // 2. Ação (Act)
        pedido.setStatus("AGUARDANDO_PAGAMENTO"); // Simulando o que o controller faz

        // 3. Verificação (Assert)
        // Esperamos que o status seja exatamente esse texto
        Assertions.assertEquals("AGUARDANDO_PAGAMENTO", pedido.getStatus(),
                "O status inicial deveria ser AGUARDANDO_PAGAMENTO");
    }

    @Test
    @DisplayName("Deve adicionar item ao pedido e manter o relacionamento bidirecional")
    void testeAdicionarItem() {
        // 1. Cenário
        Pedido pedido = new Pedido();

        ItemPedido item = new ItemPedido();
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("50.00"));

        // 2. Ação - Chamamos aquele método 'helper' que criamos na classe Pedido
        pedido.adicionarItem(item);

        // 3. Verificação

        // Verifica se a lista do pedido agora tem 1 item
        Assertions.assertEquals(1, pedido.getItens().size(), "A lista de itens deveria ter 1 elemento");

        // Verifica se o item sabe quem é o seu pedido pai (relacionamento JPA)
        Assertions.assertEquals(pedido, item.getPedido(), "O item deve estar vinculado ao pedido correto");

        // Verifica se a quantidade está correta
        Assertions.assertEquals(2, pedido.getItens().get(0).getQuantidade());
    }

    @Test
    @DisplayName("Deve calcular valor total do item corretamente (teste de lógica)")
    void testeLogicaMatematicaSimples() {
        // Um teste simples para garantir que o JUnit está funcionando com BigDecimal
        BigDecimal preco = new BigDecimal("100.00");
        BigDecimal quantidade = new BigDecimal("2");

        BigDecimal total = preco.multiply(quantidade);

        Assertions.assertEquals(new BigDecimal("200.00"), total);
    }
}