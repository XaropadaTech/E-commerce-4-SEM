package com.pi.projeto_quarto_semestre.validation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ProdutoValidator {

    private ProdutoValidator() {}

    public static Map<String, String> validar(
            String nome,
            Double avaliacao,
            String descricaoDetalhada,
            Double preco,
            Integer qtdEstoque
    ) {
        Map<String, String> erros = new LinkedHashMap<>();

        // nome: obrigatório, <= 200
        String n = nome == null ? "" : nome.trim();
        if (n.isEmpty()) {
            erros.put("nome", "Nome é obrigatório.");
        } else if (n.length() > 200) {
            erros.put("nome", "Nome deve ter no máximo 200 caracteres.");
        }

        // avaliação: obrigatório? (se seu form exigir) — 1.0..5.0 em passo 0.5
        if (avaliacao == null) {
            erros.put("avaliacao", "Avaliação é obrigatória.");
        } else {
            BigDecimal a = BigDecimal.valueOf(avaliacao);
            boolean faixa = a.compareTo(new BigDecimal("1.0")) >= 0
                    && a.compareTo(new BigDecimal("5.0")) <= 0;
            boolean passo = a.remainder(new BigDecimal("0.5")).compareTo(BigDecimal.ZERO) == 0;
            if (!faixa || !passo) {
                erros.put("avaliacao", "Avaliação deve estar entre 1,0 e 5,0 em passos de 0,5.");
            }
        }

        // descrição detalhada: <= 2000
        if (descricaoDetalhada != null && descricaoDetalhada.length() > 2000) {
            erros.put("descricaoDetalhada", "Descrição deve ter no máximo 2000 caracteres.");
        }

        // preço: obrigatório, >=0, 2 casas decimais
        if (preco == null) {
            erros.put("preco", "Preço é obrigatório.");
        } else {
            BigDecimal p = BigDecimal.valueOf(preco);
            if (p.compareTo(BigDecimal.ZERO) < 0) {
                erros.put("preco", "Preço não pode ser negativo.");
            } else {
                try {
                    // lança exceção se tiver mais de 2 casas
                    p.setScale(2, RoundingMode.UNNECESSARY);
                } catch (ArithmeticException ex) {
                    erros.put("preco", "Preço deve ter no máximo 2 casas decimais.");
                }
            }
        }

        // quantidade: obrigatória, inteiro >= 0
        if (qtdEstoque == null) {
            erros.put("qtdEstoque", "Quantidade em estoque é obrigatória.");
        } else if (qtdEstoque < 0) {
            erros.put("qtdEstoque", "Quantidade em estoque não pode ser negativa.");
        }

        return erros;
    }

    /** Normaliza entradas antes de persistir (trim/scale) */
    public static String normalizarNome(String nome) {
        return nome == null ? null : nome.trim();
    }
    public static String normalizarDescricao(String desc) {
        return desc == null ? "" : desc;
    }
    public static BigDecimal normalizarPreco(Double preco) {
        if (preco == null) return null;
        return BigDecimal.valueOf(preco).setScale(2, RoundingMode.HALF_UP);
    }
    public static BigDecimal normalizarAvaliacao(Double avaliacao) {
        if (avaliacao == null) return null;
        return BigDecimal.valueOf(avaliacao).setScale(1, RoundingMode.HALF_UP);
    }
}
