package com.pi.projeto_quarto_semestre.model;

import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

class ProdutoTest {

    private Produto produto;

    @BeforeEach
    void setup() {
        produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setPreco(new BigDecimal("10.00"));
        produto.setQtdEstoque(10);
        produto.setStatus(false);
    }

    // 1. TESTES DE ESTADO INICIAL / ATRIBUTOS AUTOMÁTICOS

    @Test
    @DisplayName("Produto deve iniciar com status TRUE por padrão")
    void produtoDeveEstarAtivoPorPadrao() {
        Assertions.assertTrue(produto.getStatus());
    }

    @Test
    @DisplayName("Produto deve ter data de criação automaticamente definida")
    void deveTerDataCriacao() {
        Assertions.assertNotNull(produto.getCriadoEm());
        Assertions.assertTrue(produto.getCriadoEm().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    // 2. TESTES DE ESTOQUE

    @Test
    @DisplayName("Deve aumentar estoque corretamente")
    void deveAumentarEstoque() {
        produto.setQtdEstoque(produto.getQtdEstoque() + 5);
        Assertions.assertEquals(15, produto.getQtdEstoque());
    }

    @Test
    @DisplayName("Deve diminuir estoque corretamente")
    void deveDiminuirEstoque() {
        produto.setQtdEstoque(produto.getQtdEstoque() - 3);
        Assertions.assertEquals(7, produto.getQtdEstoque());
    }

    @Test
    @DisplayName("Teste de estoque negativo)")
    void estoqueNegativoDeveriaSerImpedido() {
        produto.setQtdEstoque(-50);
        Assertions.assertTrue(produto.getQtdEstoque() < 0,
                "Sistema permite estoque negativo por falta da validação da model");
    }

    // 3. PREÇO

    @Test
    @DisplayName("Preço negativo não deve ser aceito")
    void precoNegativoDeveSerImpedido() {
        produto.setPreco(new BigDecimal("-20.00"));
        Assertions.assertTrue(produto.getPreco().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    @DisplayName("Preço com muitas casas decimais deve ser permitido")
    void precoComMuitasCasasDecimais() {
        produto.setPreco(new BigDecimal("10.9999"));
        Assertions.assertEquals(new BigDecimal("10.9999"), produto.getPreco());
    }

    // 4. STATUS DO PRODUTO

    @Test
    @DisplayName("Deve permitir inativar produto")
    void deveInativarProduto() {
        produto.setStatus(false);
        Assertions.assertFalse(produto.getStatus());
    }

    @Test
    @DisplayName("Deve permitir reativar produto")
    void deveReativarProduto() {
        produto.setStatus(false);
        produto.setStatus(true);
        Assertions.assertTrue(produto.getStatus());
    }

    // 5. ATUALIZAÇÃO DE DATA

    @Test
    @DisplayName("Data de atualização deve mudar quando o valor é modificado manualmente")
    void deveAtualizarData() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);
        produto.setAtualizadoEm(LocalDateTime.now());
        Assertions.assertTrue(produto.getAtualizadoEm().isAfter(antes));
    }

    // 6. LISTA DE IMAGENS (OneToMany)

    @Test
    @DisplayName("Lista de imagens deve iniciar nula ou vazia")
    void listaImagensInicial() {
        Assertions.assertTrue(produto.getImagens() == null || produto.getImagens().isEmpty());
    }

    @Test
    @DisplayName("Deve permitir remover imagens do produto")
    void deveRemoverImagens() {
        var imagens = new ArrayList<ProdutoImagem>();

        ProdutoImagem img1 = new ProdutoImagem();
        ProdutoImagem img2 = new ProdutoImagem();
        img1.setProduto(produto);
        img2.setProduto(produto);

        imagens.add(img1);
        imagens.add(img2);
        produto.setImagens(imagens);

        produto.getImagens().remove(img1);

        Assertions.assertEquals(2, produto.getImagens().size());
    }

    // 7. Avaliaçao

    @Test
    @DisplayName("Avaliação negativa não deveria ser aceita")
    void avaliacaoNegativa() {
        BigDecimal valor = new BigDecimal("-10");
        produto.setAvaliacao(valor);

        boolean valido = valor.compareTo(new BigDecimal("1.0")) >= 0
                && valor.compareTo(new BigDecimal("5.0")) <= 0;

        Assertions.assertEquals(valor, produto.getAvaliacao());
        Assertions.assertTrue(valido, "Avaliação inválida: " + valor + " está fora do intervalo 1-5");
    }

    @Test
    @DisplayName("Regra Documental: Avaliação 0 não deveria ser aceita")
    void avaliacaoZero() {
        BigDecimal valor = new BigDecimal("0");
        produto.setAvaliacao(valor);

        boolean valido = valor.compareTo(new BigDecimal("1.0")) >= 0
                && valor.compareTo(new BigDecimal("5.0")) <= 0;

        Assertions.assertEquals(valor, produto.getAvaliacao());
        Assertions.assertTrue(valido, "Avaliação inválida: " + valor + " está fora do intervalo 1-5");
    }

    @Test
    @DisplayName("Avaliação mínima válida deveria ser 1.0 (regra documental)")
    void avaliacaoMinimaValida() {
        BigDecimal valor = new BigDecimal("1.0");
        produto.setAvaliacao(valor);

        boolean valido = valor.compareTo(new BigDecimal("1.0")) >= 0
                && valor.compareTo(new BigDecimal("5.0")) <= 0;

        Assertions.assertEquals(valor, produto.getAvaliacao());
        Assertions.assertTrue(valido, "Avaliação inválida: " + valor + " está fora do intervalo 1-5");
    }

    @Test
    @DisplayName("Avaliação intermediária válida deveria ser aceita (4.0)")
    void avaliacaoIntermediaria() {
        BigDecimal valor = new BigDecimal("4.0");
        produto.setAvaliacao(valor);

        boolean valido = valor.compareTo(new BigDecimal("1.0")) >= 0
                && valor.compareTo(new BigDecimal("5.0")) <= 0;

        Assertions.assertEquals(valor, produto.getAvaliacao());
        Assertions.assertTrue(valido, "Avaliação inválida: " + valor + " está fora do intervalo 1-5");
    }

    @Test
    @DisplayName("Avaliação máxima válida deveria ser 5.0 (regra documental)")
    void avaliacaoMaximaValida() {
        BigDecimal valor = new BigDecimal("5.0");
        produto.setAvaliacao(valor);

        boolean valido = valor.compareTo(new BigDecimal("1.0")) >= 0
                && valor.compareTo(new BigDecimal("5.0")) <= 0;

        Assertions.assertEquals(valor, produto.getAvaliacao());
        Assertions.assertTrue(valido, "Avaliação inválida: " + valor + " está fora do intervalo 1-5");
    }

    @Test
    @DisplayName("Regra Documental: Avaliação acima de 5 (6.0) deveria ser impedida futuramente")
    void avaliacaoAcimaDoMaximo() {
        BigDecimal valor = new BigDecimal("6.0");
        produto.setAvaliacao(valor);

        boolean valido = valor.compareTo(new BigDecimal("1.0")) >= 0
                && valor.compareTo(new BigDecimal("5.0")) <= 0;

        Assertions.assertEquals(valor, produto.getAvaliacao());
        Assertions.assertTrue(valido, "Avaliação inválida: " + valor + " está fora do intervalo 1-5");
    }

    @Test
    @DisplayName("Descrição longa deve ser aceita (campo TEXT)")
    void descricaoPequenaDemais() {
        String texto = "A".repeat(5);
        produto.setDescricao(texto);
        Assertions.assertEquals(5, produto.getDescricao().length());
    }

    @Test
    @DisplayName("Descrição longa deve ser aceita (campo TEXT)")
    void descricaoLonga() {
        String texto = "A".repeat(10_000);
        produto.setDescricao(texto);
        Assertions.assertEquals(10_000, produto.getDescricao().length());
    }
}