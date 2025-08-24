package com.pi.projeto_quarto_semestre.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.pi.projeto_quarto_semestre.model.Usuario;
import com.pi.projeto_quarto_semestre.repository.UsuarioRepository;


@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String home() {
        return "index";  // nome da view (ex: index.html ou index.jsp)
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/autenticar")
public String autenticar(@RequestParam String email,
                         @RequestParam String senha,
                         Model model,
                         HttpSession session) {

    // Busca o usuário pelo email
    Usuario usuario = usuarioRepository.findByEmail(email);

    // Verifica se o usuário existe e se a senha está correta
    if (usuario != null && usuario.getSenha().equals(senha)) {
        // Salva na sessão
        session.setAttribute("emailUsuario", email);
        session.setAttribute("grupoUsuario", usuario.getGrupo().name());

        // Redireciona para a mesma página para ambos os perfis
        return "redirect:/paginabko";
    }

    // Caso contrário, erro de login
    model.addAttribute("erro", "Email ou senha inválidos.");
    return "login";
}

@GetMapping("/paginabko")
public String mostrarPaginaBkO(HttpSession session, Model model) {
    String grupo = (String) session.getAttribute("grupoUsuario"); // mesmo nome usado no login
    if (grupo == null) {
        return "redirect:/login";  // se não estiver logado, redireciona para login
    }

    model.addAttribute("grupo", grupo);

    return "paginabko";
}

}