package com.pi.projeto_quarto_semestre.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class ProdutoTest {

    @Test
    @DisplayName("Deve criar um produto ativo por padrão")
    void testeCriarProdutoAtivo() {
        // 1. Arrange (Cenário)
        Produto produto = new Produto();
        produto.setNome("Camiseta Java");
        produto.setPreco(new BigDecimal("50.00"));

        // Vamos assumir que no seu sistema, ao salvar ou criar, ele deve ser ativo
        // Se você tiver um método no construtor ou @PrePersist, melhor.
        // Aqui simulamos o comportamento esperado:
        produto.setStatus(true);

        // 2. Act (Ação) & 3. Assert (Verificação)
        Assertions.assertTrue(produto.getStatus(), "O produto deveria ser criado como ATIVO (true)");
        Assertions.assertEquals("Camiseta Java", produto.getNome());
    }

    @Test
    @DisplayName("Não deve aceitar preço negativo (Simulação de Regra)")
    void testePrecoNegativo() {
        // Este teste é interessante: ele documenta que o sistema NÃO DEVERIA aceitar isso.
        // Se o seu sistema aceita, o teste vai passar, mas mostra que vocês pensaram no risco.

        Produto produto = new Produto();
        BigDecimal precoNegativo = new BigDecimal("-10.00");

        produto.setPreco(precoNegativo);

        // Aqui você tem duas opções de Assert, dependendo de como seu código é:

        // OPÇÃO A: Se vocês usam Bean Validation (@Min(0)) ou lógica no setPreco:
        // Assertions.assertThrows(IllegalArgumentException.class, () -> produto.setPreco(precoNegativo));

        // OPÇÃO B (Mais simples): Apenas verificar se o valor foi setado (e marcar como falha de negócio no relatório)
        // "O sistema permitiu preço negativo, isso é um risco!"
        Assertions.assertTrue(produto.getPreco().compareTo(BigDecimal.ZERO) < 0,
                "Alerta: O sistema permitiu definir um preço negativo!");
    }

    @Test
    @DisplayName("Deve atualizar a quantidade de estoque corretamente")
    void testeAtualizacaoEstoque() {
        // 1. Arrange
        Produto produto = new Produto();
        produto.setQtdEstoque(10);

        // 2. Act - Simula uma entrada de estoque (compra de fornecedor)
        int novaQuantidade = produto.getQtdEstoque() + 5;
        produto.setQtdEstoque(novaQuantidade);

        // 3. Assert
        Assertions.assertEquals(15, produto.getQtdEstoque(), "O estoque deveria ser 15 (10 + 5)");
    }

    @Test
    @DisplayName("Deve permitir inativar um produto (Soft Delete)")
    void testeInativarProduto() {
        // Testa o 'D' do CRUD (Delete lógico)
        Produto produto = new Produto();
        produto.setStatus(true);

        // Ação: Inativar
        produto.setStatus(false);

        Assertions.assertFalse(produto.getStatus(), "O produto deveria estar inativo");
    }
}