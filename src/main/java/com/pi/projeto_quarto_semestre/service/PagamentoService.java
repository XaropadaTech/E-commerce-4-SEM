package com.pi.projeto_quarto_semestre.service;

import org.springframework.stereotype.Service;

import com.pi.projeto_quarto_semestre.model.Pagamento;
import com.pi.projeto_quarto_semestre.validation.PagamentoValidator;

@Service
public class PagamentoService {

    public void processar(Pagamento pagamento) {
        if (!PagamentoValidator.validarPagamento(pagamento)) {
            throw new IllegalArgumentException("Dados de pagamento inválidos.");
        }
    }
}