package com.pi.projeto_quarto_semestre.validation;

import com.pi.projeto_quarto_semestre.model.FormaPagamento;
import com.pi.projeto_quarto_semestre.model.Pagamento;
import com.pi.projeto_quarto_semestre.model.PagamentoCartao;
import com.pi.projeto_quarto_semestre.util.NomeUtils;

public final class PagamentoValidator {

    private PagamentoValidator() {
    }

    public static boolean validarPagamento(Pagamento pagamento) {
        
        if (pagamento == null || pagamento.getFormaPagamento() == null) {
            return false;
        }

        FormaPagamento f = pagamento.getFormaPagamento();

        if (f == FormaPagamento.BOLETO) {
            return true;
        }

        if (f == FormaPagamento.CARTAO_CREDITO || f == FormaPagamento.CARTAO_DEBITO) {
            return validarPagamentoCartao(pagamento.getPagamentoCartao());
        }

        return false;
    }

    private static boolean validarPagamentoCartao(PagamentoCartao c) {
        if (c == null) return false;

        String numero = c.getNumeroCartao();
        String cvv = c.getCvv();
        String nome = c.getNomeTitular();
        String venc = c.getDataVencimento();
        Integer parcelas = c.getNumeroParcelas();

        if (numero == null || numero.replaceAll("\\D", "").length() < 13) return false;
        if (cvv == null || !(cvv.length() == 3 || cvv.length() == 4)) return false;
        if (nome == null || !NomeUtils.nomeValido(nome)) return false;
        if (venc == null || venc.isBlank()) return false;
        if (parcelas == null || parcelas < 1) return false;

        return true;
    }
}
