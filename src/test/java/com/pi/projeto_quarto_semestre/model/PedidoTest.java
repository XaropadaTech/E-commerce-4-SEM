package com.pi.projeto_quarto_semestre.model;

import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Pedido pedido;

    @BeforeEach
    void setup() {
        pedido = new Pedido();
        pedido.setStatus("AGUARDANDO_PAGAMENTO");
        pedido.setFormaPagamento("CARTAO_CREDITO");
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setValorTotal(BigDecimal.ZERO);
    }

    // 1️⃣ Status inicial
    @Test
    @DisplayName("Pedido inicia com status AGUARDANDO_PAGAMENTO")
    void testeStatusInicial() {
        assertEquals("AGUARDANDO_PAGAMENTO", pedido.getStatus());
    }

    @Test
    @DisplayName("Alterar status do pedido")
    void testeAlterarStatus() {
        String[] statusPossiveis = {"AGUARDANDO_PAGAMENTO", "PAGO", "ENVIADO", "CANCELADO"};
        for (String s : statusPossiveis) {
            pedido.setStatus(s);
            assertEquals(s, pedido.getStatus());
        }
    }

    // 2️⃣ Forma de pagamento
    @Test
    @DisplayName("Formas de pagamento válidas")
    void testeFormaPagamento() {
        String[] formas = {"CARTAO_CREDITO", "BOLETO", "PIX"};
        for (String f : formas) {
            pedido.setFormaPagamento(f);
            assertEquals(f, pedido.getFormaPagamento());
        }
    }

    // 3️⃣ Itens do pedido - relacionamento e lógica
    @Test
    @DisplayName("Adicionar item ao pedido e verificar bidirecionalidade")
    void testeAdicionarItem() {
        ItemPedido item = new ItemPedido();
        item.setQuantidade(2);
        item.setPrecoUnitario(new BigDecimal("50.00"));

        pedido.adicionarItem(item);

        assertEquals(1, pedido.getItens().size());
        assertEquals(pedido, item.getPedido());
        assertEquals(2, pedido.getItens().get(0).getQuantidade());
    }

    @Test
    @DisplayName("Adicionar múltiplos itens e calcular valor total")
    void testeItensMultiplesValoresLimites() {
        BigDecimal totalEsperado = BigDecimal.ZERO;

        for (int i = 1; i <= 5; i++) {
            ItemPedido item = new ItemPedido();
            item.setQuantidade(i);
            BigDecimal preco = new BigDecimal(i * 10); // 10, 20, 30, 40, 50
            item.setPrecoUnitario(preco);

            pedido.adicionarItem(item);
            totalEsperado = totalEsperado.add(preco.multiply(BigDecimal.valueOf(i)));
        }

        // Simula cálculo do total
        BigDecimal totalCalculado = pedido.getItens().stream()
                .map(it -> it.getPrecoUnitario().multiply(BigDecimal.valueOf(it.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(5, pedido.getItens().size());
        assertEquals(totalEsperado, totalCalculado);
    }

    @Test
    @DisplayName("Adicionar item com quantidade zero ou negativa")
    void testeItemQuantidadeLimite() {
        ItemPedido itemNegativo = new ItemPedido();
        itemNegativo.setQuantidade(-1);
        itemNegativo.setPrecoUnitario(new BigDecimal("100.00"));
        pedido.adicionarItem(itemNegativo);

        assertEquals(-1, pedido.getItens().get(0).getQuantidade(),
                "Sistema permite quantidade negativa, o que seria um valor limite extremo");

        ItemPedido itemZero = new ItemPedido();
        itemZero.setQuantidade(0);
        itemZero.setPrecoUnitario(new BigDecimal("50.00"));
        pedido.adicionarItem(itemZero);

        assertEquals(0, pedido.getItens().get(1).getQuantidade());
    }

    // 4️⃣ Data do pedido - valores limites
    @Test
    @DisplayName("Data do pedido atual e limites extremos")
    void testeDataPedidoLimites() {
        LocalDateTime agora = LocalDateTime.now();
        pedido.setDataPedido(agora);
        assertTrue(pedido.getDataPedido().isBefore(LocalDateTime.now().plusSeconds(1)));

        LocalDateTime passadoExtremo = LocalDateTime.of(2000, 1, 1, 0, 0);
        pedido.setDataPedido(passadoExtremo);
        assertEquals(passadoExtremo, pedido.getDataPedido());

        LocalDateTime futuroExtremo = LocalDateTime.now().plus(100, ChronoUnit.YEARS);
        pedido.setDataPedido(futuroExtremo);
        assertEquals(futuroExtremo, pedido.getDataPedido());
    }

    // 5️⃣ Testes combinando múltiplos cenários (complexidade ciclomatica)
    @Test
    @DisplayName("Cenários combinados de status, pagamento, itens e datas")
    void testeCiclomatico() {
        // Cenário 1: status PAGO, pagamento PIX, 1 item
        pedido.setStatus("PAGO");
        pedido.setFormaPagamento("PIX");
        ItemPedido item1 = new ItemPedido();
        item1.setQuantidade(1);
        item1.setPrecoUnitario(new BigDecimal("100"));
        pedido.adicionarItem(item1);
        assertEquals("PAGO", pedido.getStatus());
        assertEquals("PIX", pedido.getFormaPagamento());
        assertEquals(1, pedido.getItens().size());

        // Cenário 2: status ENVIADO, pagamento BOLETO, 2 itens
        pedido.setStatus("ENVIADO");
        pedido.setFormaPagamento("BOLETO");
        ItemPedido item2 = new ItemPedido();
        item2.setQuantidade(2);
        item2.setPrecoUnitario(new BigDecimal("50"));
        pedido.adicionarItem(item2);
        assertEquals(2, pedido.getItens().size());
        assertEquals("ENVIADO", pedido.getStatus());
    }

    // 6️⃣ Verificação do método dataPedidoFormatada
    @Test
    @DisplayName("Verificar dataPedidoFormatada")
    void testeDataFormatada() {
        LocalDateTime data = LocalDateTime.of(2025, 11, 24, 14, 30);
        pedido.setDataPedido(data);

        String formatada = pedido.getDataPedidoFormatada();
        assertEquals("24/11/2025 14:30", formatada);
    }

    // 7️⃣ Teste de valor total negativo ou zero
    @Test
    @DisplayName("Valor total do pedido negativo ou zero")
    void testeValorTotalLimite() {
        // Valor total zero
        pedido.setValorTotal(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, pedido.getValorTotal(), "Valor total igual a zero deve ser permitido");

        // Valor total negativo (não deveria ocorrer em produção, mas teste de limite)
        BigDecimal negativo = new BigDecimal("-100.00");
        pedido.setValorTotal(negativo);
        assertEquals(negativo, pedido.getValorTotal(), "Sistema permite valor total negativo, limite extremo");
    }

    // 8️⃣ Cenário de pedido com 0 itens
    @Test
    @DisplayName("Pedido com 0 itens")
    void testePedidoSemItens() {
        pedido.setItens(new ArrayList<>()); // garante que não há itens
        assertTrue(pedido.getItens().isEmpty(), "Pedido sem itens deve ter lista vazia");

        // Valor total deveria idealmente ser zero
        pedido.setValorTotal(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, pedido.getValorTotal(), "Valor total de pedido sem itens deve ser zero");
    }
}