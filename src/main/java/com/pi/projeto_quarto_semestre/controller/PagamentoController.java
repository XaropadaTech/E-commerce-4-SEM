package com.pi.projeto_quarto_semestre.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pi.projeto_quarto_semestre.model.Pagamento;
import com.pi.projeto_quarto_semestre.service.PagamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pagamento")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/processar")
    public ResponseEntity<?> processar(@Valid @RequestBody Pagamento pagamento) {
        try {
            pagamentoService.processar(pagamento);
            return ResponseEntity.ok().body("Pagamento válido e processado (stub).");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Erro interno ao processar pagamento.");
        }
    }
}