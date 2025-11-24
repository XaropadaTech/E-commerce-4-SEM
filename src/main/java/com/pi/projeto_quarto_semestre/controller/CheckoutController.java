package com.pi.projeto_quarto_semestre.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.pi.projeto_quarto_semestre.dto.CheckoutSessionDTO;
import com.pi.projeto_quarto_semestre.model.Cliente;
import com.pi.projeto_quarto_semestre.model.Endereco;
import com.pi.projeto_quarto_semestre.repository.ClienteRepository;
import com.pi.projeto_quarto_semestre.repository.EnderecoRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository; // ✅ para buscar endereço de entrega

    public CheckoutController(ClienteRepository clienteRepository,
                              EnderecoRepository enderecoRepository) {
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
    }

    // helper para URL-encode das mensagens
    private String url(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

@PostMapping("/selecionar-endereco")
@ResponseBody
public Map<String, Object> selecionarEndereco(@RequestParam("enderecoId") Long enderecoId,
                                              HttpSession session) {
    Long clienteId = (Long) session.getAttribute("clienteId");
    if (clienteId == null) return Map.of("ok", false, "msg", "Sem login");
    session.setAttribute("checkout_enderecoEntregaId", enderecoId);
    return Map.of("ok", true);
}


    @PostMapping("/adicionar-endereco")
    @Transactional
    public String adicionarEndereco(@ModelAttribute Endereco enderecoForm, HttpSession session) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login&next=/checkout/start";
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/cliente/auth?tab=login&erro=" + url("Sessão expirada.");
        }

        Cliente cliente = clienteOpt.get();

        // Limpa o CEP e configura corretamente o tipo e relação
        enderecoForm.setCep(enderecoForm.getCep().replaceAll("\\D", ""));
        enderecoForm.setTipo(Endereco.Tipo.ENTREGA);
        enderecoForm.setCliente(cliente);

        cliente.getEnderecos().add(enderecoForm);
        clienteRepository.save(cliente);

        return "redirect:/checkout/start?ok=" + url("Novo endereço adicionado!");
    }

    @GetMapping("/start")
    public String start(HttpSession session, Model model) {
        Long clienteId = (Long) session.getAttribute("clienteId");

        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login&next=/checkout/start";
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/cliente/auth?tab=login&erro=Sessão expirada.";
        }

        Cliente cliente = clienteOpt.get();

        // Adiciona cliente no model para o Thymeleaf
        model.addAttribute("cliente", cliente);

        // 🔹 Adiciona cliente na sessão para checkout/pagamento
        session.setAttribute("cliente", cliente);

        // Você pode manter esta tela inicial ou redirecionar direto para /checkout/pagamento
        return "checkout-start";
        // return "redirect:/checkout/pagamento";
    }

    @PostMapping("/perfil/salvar")
    @Transactional
    public String salvarPerfil(@ModelAttribute Cliente clienteForm, HttpSession session) {

        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            return "redirect:/auth?tab=login&erro=" + url("Sessão expirada.");
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            return "redirect:/auth?tab=login&erro=" + url("Cliente não encontrado.");
        }

        Cliente cliente = clienteOpt.get();

        // ---------------- Endereços ----------------
        cliente.getEnderecos().clear();
        if (clienteForm.getEnderecos() != null) {
            for (Endereco e : clienteForm.getEnderecos()) {
                if (e == null) continue;

                // remove caracteres não numéricos do CEP
                if (e.getCep() != null) {
                    e.setCep(e.getCep().replaceAll("\\D", ""));
                }

                e.setCliente(cliente);
                cliente.getEnderecos().add(e);
            }
        }

        clienteRepository.save(cliente);
        session.setAttribute("clienteEmail", cliente.getEmail());

        return "redirect:/cliente/perfilCliente?ok=" + url("Perfil atualizado com sucesso!");
    }

    // ============================================================
    // ✅ NOVO: TELA DE PAGAMENTO (GET) E SALVAR PAGAMENTO (POST)
    // ============================================================

    @GetMapping("/pagamento")
    public String checkoutPagamento(Model model, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cliente/auth?tab=login&next=/checkout/pagamento";
        }
        model.addAttribute("cliente", cliente);
        return "checkout-pagamento"; // arquivo raiz em /templates
    }

    // Salva forma de pagamento na sessão
    @PostMapping("/pagamento")
public String salvarPagamento(@RequestParam String metodo,
                              @RequestParam(required = false) String nomeCompleto,
                              @RequestParam(required = false) String numeroCartao,
                              @RequestParam(required = false) String validade,
                              @RequestParam(required = false) String parcelas,
                              HttpSession session) {

    Long clienteId = (Long) session.getAttribute("clienteId");
    if (clienteId == null) {
        return "redirect:/cliente/auth?tab=login&next=/checkout/pagamento";
    }

    var pag = new CheckoutSessionDTO.Pagamento();
    pag.setMetodo(metodo);

    if ("CARTAO".equals(metodo)) {
        pag.setNomeCompleto(nomeCompleto);
        String ult4 = (numeroCartao != null && numeroCartao.replaceAll("\\D","").length() >= 4)
                ? numeroCartao.replaceAll("\\D","")
                  .substring(numeroCartao.replaceAll("\\D","").length() - 4)
                : "****";
        pag.setNumeroCartao("**** **** **** " + ult4);
        pag.setValidade(validade);
        pag.setParcelas(parcelas);
    }
    // BOLETO e PIX não exigem campos adicionais aqui
    session.setAttribute("checkout_pagamento", pag);

    return "redirect:/checkout/resumo";
}


    // ============================================================
    // ✅ NOVO: SINCRONIZAR CARRINHO (localStorage → sessão)
    // ============================================================

    @PostMapping("/sync-cart")
    @ResponseBody
    public Map<String, Object> syncCart(@RequestBody CheckoutSessionDTO payload, HttpSession session) {
        session.setAttribute("checkout_itens", payload.getItens());
        session.setAttribute("checkout_frete", payload.getFrete() != null ? payload.getFrete() : BigDecimal.ZERO);
        return Map.of("ok", true);
    }



    
    // ============================================================
    // ✅ NOVO: RESUMO (usa validar-pedido-final.html no /templates)
    // ============================================================

@GetMapping("/resumo")
public String resumo(Model model, HttpSession session) {
    Long clienteId = (Long) session.getAttribute("clienteId");
    if (clienteId == null) return "redirect:/cliente/auth?tab=login&next=/checkout/resumo";

    @SuppressWarnings("unchecked")
    List<CheckoutSessionDTO.Item> itens =
        (List<CheckoutSessionDTO.Item>) session.getAttribute("checkout_itens");
   
        Object freteObj = session.getAttribute("checkout_frete");
BigDecimal frete = BigDecimal.ZERO;
if (freteObj instanceof BigDecimal b) frete = b;
else if (freteObj instanceof Number n) frete = BigDecimal.valueOf(n.doubleValue());
else if (freteObj instanceof String s) {
    try { frete = new BigDecimal(s.replace(",", ".")); } catch (Exception ignored) {}
}

    // 🔹 ENDEREÇO
    Long enderecoId = (Long) session.getAttribute("checkout_enderecoEntregaId");
    Endereco enderecoEntrega = null;
    if (enderecoId != null) {
        enderecoEntrega = enderecoRepository.findById(enderecoId).orElse(null);
    }
    if (enderecoEntrega == null) {
        // tente buscar padrão do cliente, ou o primeiro de ENTREGA
        // (implemente no repository se ainda não houver)
        enderecoEntrega = enderecoRepository.findByClienteIdAndPadraoTrue(clienteId);
        if (enderecoEntrega == null) {
            enderecoEntrega = enderecoRepository
               .findFirstByClienteIdAndTipoOrderByIdAsc(clienteId, Endereco.Tipo.ENTREGA);
        }
    }

    CheckoutSessionDTO.Pagamento pagamento =
        (CheckoutSessionDTO.Pagamento) session.getAttribute("checkout_pagamento");

    BigDecimal subtotal = BigDecimal.ZERO;
    if (itens != null) {
        for (var it : itens) {
            if (it != null && it.getPrecoUnit() != null && it.getQuantidade() != null) {
                subtotal = subtotal.add(it.getPrecoUnit()
                        .multiply(BigDecimal.valueOf(it.getQuantidade())));
            }
        }
    }
    BigDecimal totalGeral = subtotal.add(frete == null ? BigDecimal.ZERO : frete);

    model.addAttribute("itens", itens);
    model.addAttribute("subtotal", subtotal);
    model.addAttribute("frete", frete);
    model.addAttribute("totalGeral", totalGeral);
    model.addAttribute("enderecoEntrega", enderecoEntrega);
    model.addAttribute("pagamento", pagamento);

    return "validar-pedido-final";
}


    // Voltar do resumo → pagamento
    @PostMapping("/resumo/back")
    public String voltarPagamento(HttpSession session) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login&next=/checkout/pagamento";
        }
        return "redirect:/checkout/pagamento";
    }

    // Concluir compra → segue para confirmação (placeholder)
    @PostMapping("/resumo/concluir")
    public String concluirCompra(HttpSession session) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login&next=/checkout/resumo";
        }
        // Próximo passo (se quiser): criar Pedido no banco e limpar chaves da sessão.
        return "redirect:/checkout/confirmacao";
    }

    // Confirmação simples
    @GetMapping("/confirmacao")
    public String confirmacao(HttpSession session, Model model) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login";
        }
        model.addAttribute("mensagem", "Pedido concluído! (confirmação simples)");
        return "checkout-confirmacao"; // arquivo raiz no /templates
    }

    // ============================================================
    // (Mantive seu GET /validar para compatibilidade, mas agora usamos /resumo)
    // ============================================================
    @GetMapping("/validar")
public String validarPedidoFinal(Model model, HttpSession session) {
    // Reaproveita a mesma lógica do resumo, que já calcula itens, subtotal, frete e totalGeral
    return resumo(model, session);
}

}
