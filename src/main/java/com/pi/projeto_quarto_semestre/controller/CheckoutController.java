package com.pi.projeto_quarto_semestre.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @GetMapping("/start")
    public String start(HttpSession session) {
        Long clienteId = (Long) session.getAttribute("clienteId");

        // ❗ Se não estiver logado, redireciona para o login do cliente
        // e guarda o destino (next) para voltar ao checkout depois
        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login&next=/checkout/start";
        }

        // ✅ Logado -> mostra o template que está em
        // src/main/resources/templates/checkout-start.html
        return "checkout-start";
    }
}

