package com.pi.projeto_quarto_semestre.controller;

import com.pi.projeto_quarto_semestre.model.Cliente;
import com.pi.projeto_quarto_semestre.model.Endereco;
import com.pi.projeto_quarto_semestre.model.Endereco.Tipo;
import com.pi.projeto_quarto_semestre.repository.ClienteRepository;
import com.pi.projeto_quarto_semestre.service.ClienteService;


import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteController(ClienteService clienteService,
                             ClienteRepository clienteRepository,
                             PasswordEncoder passwordEncoder) {
        this.clienteService = clienteService;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET: página única (login + cadastro)
    @GetMapping("/auth")
    public String authPage(
            @RequestParam(value = "tab", required = false, defaultValue = "login") String tab,
            Model model) {
        model.addAttribute("tab", tab);
        return "cliente-auth"; // <-- AGORA sem o "cliente/" antes
    }


    // POST: cadastro
    @PostMapping("/cadastrar")
    public String cadastrar(
            @RequestParam String nomeCompleto,
            @RequestParam String email,
            @RequestParam String cpf,
            @RequestParam String dataNascimento, // yyyy-MM-dd
            @RequestParam String genero,         // MASCULINO | FEMININO | OUTRO | PREFIRO_NAO_INFORMAR
            @RequestParam String senha,
            // faturamento
            @RequestParam String fatCep,
            @RequestParam String fatLogradouro,
            @RequestParam String fatNumero,
            @RequestParam(required=false) String fatComplemento,
            @RequestParam String fatBairro,
            @RequestParam String fatCidade,
            @RequestParam String fatUf,
            // entrega 1 (obrigatória)
            @RequestParam String ent1Cep,
            @RequestParam String ent1Logradouro,
            @RequestParam String ent1Numero,
            @RequestParam(required=false) String ent1Complemento,
            @RequestParam String ent1Bairro,
            @RequestParam String ent1Cidade,
            @RequestParam String ent1Uf,
            // entrega extras (opcional, virão como arrays JS)
            @RequestParam(required=false, name="entCep[]") String[] entCep,
            @RequestParam(required=false, name="entLogradouro[]") String[] entLogradouro,
            @RequestParam(required=false, name="entNumero[]") String[] entNumero,
            @RequestParam(required=false, name="entComplemento[]") String[] entComplemento,
            @RequestParam(required=false, name="entBairro[]") String[] entBairro,
            @RequestParam(required=false, name="entCidade[]") String[] entCidade,
            @RequestParam(required=false, name="entUf[]") String[] entUf
    ) {
        try {
            Cliente c = new Cliente();
            c.setNomeCompleto(nomeCompleto);
            c.setEmail(email);
            c.setCpf(cpf);
            c.setDataNascimento(java.time.LocalDate.parse(dataNascimento));
            c.setGenero(Cliente.Genero.valueOf(genero));
            c.setSenhaHash(senha); // será encriptada no service

            // FATURAMENTO
            Endereco fat = new Endereco();
            fat.setTipo(Tipo.FATURAMENTO);
            fat.setCep(fatCep);
            fat.setLogradouro(fatLogradouro);
            fat.setNumero(fatNumero);
            fat.setComplemento(fatComplemento);
            fat.setBairro(fatBairro);
            fat.setCidade(fatCidade);
            fat.setUf(fatUf);
            c.addEndereco(fat);

            // ENTREGA obrigatória
            Endereco ent1 = new Endereco();
            ent1.setTipo(Tipo.ENTREGA);
            ent1.setCep(ent1Cep);
            ent1.setLogradouro(ent1Logradouro);
            ent1.setNumero(ent1Numero);
            ent1.setComplemento(ent1Complemento);
            ent1.setBairro(ent1Bairro);
            ent1.setCidade(ent1Cidade);
            ent1.setUf(ent1Uf);
            c.addEndereco(ent1);

            // ENTREGA extras
            if (entCep != null) {
                for (int i = 0; i < entCep.length; i++) {
                    Endereco e = new Endereco();
                    e.setTipo(Tipo.ENTREGA);
                    e.setCep(entCep[i]);
                    e.setLogradouro(entLogradouro[i]);
                    e.setNumero(entNumero[i]);
                    e.setComplemento(entComplemento != null && entComplemento.length > i ? entComplemento[i] : null);
                    e.setBairro(entBairro[i]);
                    e.setCidade(entCidade[i]);
                    e.setUf(entUf[i]);
                    c.addEndereco(e);
                }
            }

            clienteService.cadastrarNovo(c);

            // sucesso -> volta para mesma página, agora na aba de login
            return "redirect:/cliente/auth?tab=login&ok=1";

        } catch (IllegalArgumentException ex) {
            return "redirect:/cliente/auth?tab=cadastro&erro=" + url(ex.getMessage());
        } catch (Exception ex) {
            return "redirect:/cliente/auth?tab=cadastro&erro=" + url("Falha ao cadastrar.");
        }
    }

    // POST: login de cliente simples (se não estiver usando Spring Security para isso)
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String senha,
                        HttpSession session) {
        return clienteRepository.findByEmailIgnoreCase(email)
                .map(c -> {
                    if (passwordEncoder.matches(senha, c.getSenhaHash())) {
                        session.setAttribute("clienteId", c.getId());
                        session.setAttribute("clienteEmail", c.getEmail());
                        return "redirect:/"; // ou /minha-conta
                    }
                    return "redirect:/cliente/auth?tab=login&erro=" + url("Credenciais inválidas.");
                })
                .orElse("redirect:/cliente/auth?tab=login&erro=" + url("Credenciais inválidas."));
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private String url(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
