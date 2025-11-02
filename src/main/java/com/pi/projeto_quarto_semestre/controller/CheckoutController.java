package com.pi.projeto_quarto_semestre.controller;

import com.pi.projeto_quarto_semestre.model.Cliente;
import com.pi.projeto_quarto_semestre.model.Endereco;
import com.pi.projeto_quarto_semestre.repository.ClienteRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final ClienteRepository clienteRepository;

    public CheckoutController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // helper para URL-encode das mensagens
    private String url(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
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

    return "checkout-start";
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
                if (e == null)
                    continue;

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

    @GetMapping("/pagamento")
    public String checkoutPagamento(Model model, HttpSession session) {
        // Pega o cliente logado
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cliente/auth?tab=login";
        }

        // Aqui você pode carregar dados do carrinho, endereços etc.
        model.addAttribute("cliente", cliente);
        // model.addAttribute("carrinho", carrinhoService.obterCarrinho(cliente));

        return "checkout-pagamento"; // Thymeleaf vai procurar por checkout-pagamento.html
    }

}
