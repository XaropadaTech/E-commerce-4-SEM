package com.pi.projeto_quarto_semestre.controller;

import jakarta.servlet.http.HttpServletRequest;
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
        return "index"; // nome da view (ex: index.html ou index.jsp)
    }

    @GetMapping("/index")
    public String home2() {
        return "index"; // nome da view (ex: index.html ou index.jsp)
    }

@GetMapping("/login")
public String login(HttpSession session) {
    if (session.getAttribute("emailUsuario") != null) {
        // Se já estiver logado, redireciona para página principal
        return "redirect:/paginabko";
    }
    return "login";
}

@GetMapping("/autenticar")
public String autenticarGet() {
    return "redirect:/login";
}

    @PostMapping("/autenticar")
    public String autenticar(@RequestParam String email,
            @RequestParam String senha,
            Model model,
            HttpSession session,
            HttpServletRequest request) {

                if (session.getAttribute("emailUsuario") != null) {
        // Se já estiver logado, bloqueia o login
        return "redirect:/paginabko";
    }

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
        return "redirect:/login";
    }

    @GetMapping("/paginabko")
    public String mostrarPaginaBkO(HttpSession session, Model model) {
        String grupo = (String) session.getAttribute("grupoUsuario"); // mesmo nome usado no login
        if (grupo == null) {
            return "redirect:/login"; // se não estiver logado, redireciona para login
        }

        model.addAttribute("grupo", grupo);

        return "paginabko";
    }

    @GetMapping("/listaProdutos")
    public String mostrarListaProdutos(HttpSession session, Model model) {
        String grupo = (String) session.getAttribute("grupoUsuario"); // mesmo nome usado no login
        if (grupo == null) {
            return "redirect:/login"; // se não estiver logado, redireciona para login
        }

        model.addAttribute("grupo", grupo);

        return "listaProdutos";
    }

    @GetMapping("/listaUsuarios")
    public String mostrarListaUsuarios(
            @RequestParam(value = "nome", required = false) String nome, // <-- NOVO
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
public String mostrarFormularioAlterarUsuario(@RequestParam(required = false) Long id, Model model, HttpSession session) {
    if (id == null) {
        return "redirect:/login"; // Ou uma página de erro amigável
    }

    Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
    if (usuarioOpt.isPresent()) {
        model.addAttribute("usuario", usuarioOpt.get());
        // Agora você pode usar session aqui
        String emailLogado = (String) session.getAttribute("emailUsuario");
        model.addAttribute("emailLogado", emailLogado);
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
        Model model,
        HttpSession session) {

    // Pega o email do usuário logado na sessão
    String emailLogado = (String) session.getAttribute("emailUsuario");

    Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuario.getId());

    if (usuarioExistente.isPresent()) {
        Usuario u = usuarioExistente.get();

        // Normaliza CPF (opcional, caso salve só dígitos)
        String cpfNovo = usuario.getCpf() != null ? usuario.getCpf().replaceAll("\\D", "") : null;
        String emailNovo = usuario.getEmail();

        boolean mudouEmail = emailNovo != null && !emailNovo.equalsIgnoreCase(u.getEmail());
        boolean mudouCpf = cpfNovo != null && !cpfNovo.equalsIgnoreCase(u.getCpf());

        // Checagem de duplicidade se mudou o e-mail
        if (mudouEmail && usuarioRepository.existsByEmail(emailNovo)) {
            redirectAttributes.addFlashAttribute("erro", "Já existe um usuário com esse e-mail.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/alterarUsuario?id=" + usuario.getId();
        }

        // Checagem de duplicidade se mudou o CPF
        if (mudouCpf && usuarioRepository.existsByCpf(cpfNovo)) {
            redirectAttributes.addFlashAttribute("erro", "Já existe um usuário com esse CPF.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/alterarUsuario?id=" + usuario.getId();
        }

        // Atualiza dados básicos
        u.setNome(usuario.getNome());

        if (mudouCpf) {
            u.setCpf(cpfNovo);
        }
        if (mudouEmail) {
            u.setEmail(emailNovo);
        }

        // Atualização de senha (opcional)
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            if (usuario.getSenha().equals(confirmarSenha)) {
                // TODO: aplicar hash (BCrypt) aqui para maior segurança
                u.setSenha(usuario.getSenha());
            } else {
                redirectAttributes.addFlashAttribute("erro", "As senhas não coincidem.");
                return "redirect:/alterarUsuario?id=" + usuario.getId();
            }
        }

        // ALTERAÇÃO DO GRUPO: só se não for o próprio usuário logado
        if (!u.getEmail().equalsIgnoreCase(emailLogado)) {
            // Altera o grupo conforme selecionado no formulário
            u.setGrupo(usuario.getGrupo());
        }

        try {
            usuarioRepository.save(u);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário alterado com sucesso!");
            return "redirect:/listaUsuarios";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("erro", "E-mail ou CPF já cadastrado.");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/alterarUsuario?id=" + usuario.getId();
        }
    }

    redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
    return "redirect:/listaUsuarios";
}

    @PostMapping("/usuario/alterar-status")
    public String alterarStatusUsuario(@RequestParam Long id) {
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
public String salvarUsuario(@ModelAttribute("usuario") Usuario usuario, @RequestParam String confirmarSenha,
                            RedirectAttributes ra) {

    // Normalizar CPF para somente dígitos
    if (usuario.getCpf() != null) {
        usuario.setCpf(usuario.getCpf().replaceAll("\\D", ""));
    }

    // validação de e-mail duplicado
    if (usuarioRepository.existsByEmail(usuario.getEmail())) {
        ra.addFlashAttribute("erro", "Já existe um usuário com esse e-mail.");
        ra.addFlashAttribute("usuario", usuario);
        return "redirect:/cadastroUsuario";
    }

    // validação de CPF duplicado
    if (usuarioRepository.existsByCpf(usuario.getCpf())) {
        ra.addFlashAttribute("erro", "Já existe um usuário com esse CPF.");
        ra.addFlashAttribute("usuario", usuario);
        return "redirect:/cadastroUsuario";
    }

     // Validação da senha e confirmação
    if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
        ra.addFlashAttribute("erro", "A senha não pode ficar em branco.");
        ra.addFlashAttribute("usuario", usuario);
        return "redirect:/cadastroUsuario";
    } else if (!usuario.getSenha().equals(confirmarSenha)) {
        ra.addFlashAttribute("erro", "As senhas não coincidem.");
        ra.addFlashAttribute("usuario", usuario);
        return "redirect:/cadastroUsuario";
    }

    try {
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("mensagem", "Usuário cadastrado com sucesso!");
        return "redirect:/listaUsuarios";
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
        // Blindagem final para UNIQUE no BD (email/cpf)
        ra.addFlashAttribute("erro", "E-mail ou CPF já cadastrado.");
        ra.addFlashAttribute("usuario", usuario);
        return "redirect:/cadastroUsuario";
    }
}

    /**
     * Este método é chamado via JavaScript (fetch) para verificar
     * se um e-mail já está cadastrado no banco de dados.
     * A anotação @ResponseBody faz com que ele retorne um dado (true/false)
     * em vez de uma página HTML.
     */
    @GetMapping("/usuario/email-exists")
    @ResponseBody // <-- MUITO IMPORTANTE: Indica que a resposta é um dado, não uma página
    public boolean emailExists(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return usuarioRepository.existsByEmail(email);
    }


@GetMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate(); // invalida a sessão atual
    return "redirect:/login"; // redireciona para a página de login
}

}