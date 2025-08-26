package com.pi.projeto_quarto_semestre.controller;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/index")
    public String home2() {
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
    if (usuario != null && usuario.getSenha().equals(senha) && Boolean.TRUE.equals(usuario.getStatus())) {
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

@GetMapping("/listaProdutos")
public String mostrarListaProdutos(HttpSession session, Model model) {
    String grupo = (String) session.getAttribute("grupoUsuario"); // mesmo nome usado no login
    if (grupo == null) {
        return "redirect:/login";  // se não estiver logado, redireciona para login
    }

    model.addAttribute("grupo", grupo);

    return "listaProdutos";
}

@GetMapping("/listaUsuarios")
public String mostrarListaUsuarios(
        @RequestParam(value = "nome", required = false) String nome,  // <-- NOVO
        HttpSession session,
        Model model) {
    
    String grupo = (String) session.getAttribute("grupoUsuario"); 
    if (grupo == null) {
        return "redirect:/login";  
    }

    model.addAttribute("grupo", grupo);

    // Se o parâmetro nome não veio, lista todos
    List<Usuario> usuarios;
    if (nome != null && !nome.isBlank()) {
        usuarios = usuarioRepository.findByNomeContainingIgnoreCase(nome); // <-- usa o filtro
    } else {
        usuarios = usuarioRepository.findAll();
    }

    model.addAttribute("usuarios", usuarios);
    model.addAttribute("filtroNome", nome == null ? "" : nome); // <-- para preencher no input

    return "listaUsuarios";
}

@GetMapping("/alterarUsuario")
public String mostrarFormularioAlterarUsuario(@RequestParam(required = false) Long id, Model model) {
    if (id == null) {
        return "redirect:/login"; // Ou uma página de erro amigável
    }

    Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
    if (usuarioOpt.isPresent()) {
        model.addAttribute("usuario", usuarioOpt.get());
        return "alterarUsuario"; // nome do template
    } else {
        return "redirect:/listaUsuarios"; // ou página de erro
    }
}

@PostMapping("/usuario/salvar-alteracao")
public String salvarAlteracaoUsuario(
    @ModelAttribute Usuario usuario, 
    @RequestParam String confirmarSenha,
    RedirectAttributes redirectAttributes,
        Model model) {
    Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuario.getId());

    if (usuarioExistente.isPresent()) {
        Usuario u = usuarioExistente.get();

        u.setNome(usuario.getNome());
        u.setCpf(usuario.getCpf());

        // Se senha não estiver vazia e for igual à confirmação, atualiza
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            if (usuario.getSenha().equals(confirmarSenha)) {
                // Aqui você deve criptografar a senha, se necessário (ex: BCrypt)
                u.setSenha(usuario.getSenha());
            } else {
                // Senha e confirmação não batem
                redirectAttributes.addFlashAttribute("erro", "As senhas não coincidem.");
                return "redirect:/alterarUsuario?id=" + usuario.getId();
            }
        }

        usuarioRepository.save(u);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário alterado com sucesso!");
        return "redirect:/listaUsuarios";
    }

    redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
    return "redirect:/listaUsuarios";
}



@PostMapping("/usuario/alterar-status")
public String alterarStatusUsuario(@RequestParam Long  id) {
    Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);

    if (optionalUsuario.isPresent()) {
        Usuario usuario = optionalUsuario.get();
        usuario.setStatus(!usuario.getStatus()); // inverte o status atual
        usuarioRepository.save(usuario); // atualiza no banco
    }

    return "redirect:/listaUsuarios"; // recarrega a tabela
}

 // EXIBIR formulário de cadastro
    @GetMapping("/cadastroUsuario")
    public String cadastroUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastroUsuario";
    }

     // SALVAR novo usuário
    @PostMapping("/usuario")
    public String salvarUsuario(@ModelAttribute("usuario") Usuario usuario,
                                RedirectAttributes ra) {
        // Ex.: definir status padrão ativo, validar campos, etc.
        // usuario.setStatus(Boolean.TRUE);
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("mensagem", "Usuário cadastrado com sucesso!");
        return "redirect:/listaUsuarios";
    }


}