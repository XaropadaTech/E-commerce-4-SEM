package com.pi.projeto_quarto_semestre.controller;

import com.pi.projeto_quarto_semestre.model.Produto;
import com.pi.projeto_quarto_semestre.model.ProdutoImagem;
import com.pi.projeto_quarto_semestre.model.Usuario;
import com.pi.projeto_quarto_semestre.repository.ProdutoImagemRepository;
import com.pi.projeto_quarto_semestre.repository.ProdutoRepository;
import com.pi.projeto_quarto_semestre.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoImagemRepository produtoImagemRepository;


    @GetMapping("/")
    public String home(
            @RequestParam(name = "page", defaultValue = "0") int paginaAtual,
            @RequestParam(name = "nome", required = false) String filtroNome,
            Model model,
            HttpSession session) {

        // Recupera email do usuário logado na sessão
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        if (emailUsuario != null) {
            // Busca o usuário no banco pelo email
            Usuario usuario = usuarioRepository.findByEmail(emailUsuario);
            model.addAttribute("usuarioLogado", usuario);
        } else {
            model.addAttribute("usuarioLogado", null);
        }

        int tamanhoPagina = 12;
        Page<Produto> paginaProdutos;

        if (filtroNome != null && !filtroNome.isEmpty()) {
            paginaProdutos = produtoRepository.findByNomeContainingIgnoreCaseOrderByIdDesc(
                    filtroNome, PageRequest.of(paginaAtual, tamanhoPagina));
        } else {
            paginaProdutos = produtoRepository.findAllByOrderByCriadoEmDesc(
                    PageRequest.of(paginaAtual, tamanhoPagina));
        }

        model.addAttribute("produtos", paginaProdutos.getContent());
        model.addAttribute("paginaAtual", paginaAtual);
        model.addAttribute("totalPaginas", paginaProdutos.getTotalPages());
        model.addAttribute("filtroNome", filtroNome);

        return "index"; // seu template home.html ou index.html
    }

    @GetMapping("/index")
    public String home2(
            @RequestParam(name = "page", defaultValue = "0") int paginaAtual,
            @RequestParam(name = "nome", required = false) String filtroNome,
            Model model,
            HttpSession session) {

        // Recupera email do usuário logado na sessão
        String emailUsuario = (String) session.getAttribute("emailUsuario");

        if (emailUsuario != null) {
            // Busca o usuário no banco pelo email
            Usuario usuario = usuarioRepository.findByEmail(emailUsuario);
            model.addAttribute("usuarioLogado", usuario);
        } else {
            model.addAttribute("usuarioLogado", null);
        }

        int tamanhoPagina = 12;
        Page<Produto> paginaProdutos;

        if (filtroNome != null && !filtroNome.isEmpty()) {
            paginaProdutos = produtoRepository.findByNomeContainingIgnoreCaseOrderByIdDesc(
                    filtroNome, PageRequest.of(paginaAtual, tamanhoPagina));
        } else {
            paginaProdutos = produtoRepository.findAllByOrderByCriadoEmDesc(
                    PageRequest.of(paginaAtual, tamanhoPagina));
        }

        model.addAttribute("produtos", paginaProdutos.getContent());
        model.addAttribute("paginaAtual", paginaAtual);
        model.addAttribute("totalPaginas", paginaProdutos.getTotalPages());
        model.addAttribute("filtroNome", filtroNome);

        return "index"; // seu template home.html ou index.html
    }

    @GetMapping("/produto/{id}")
public String mostrarDetalhesProduto(@PathVariable Long id, Model model) {
    Optional<Produto> produtoOptional = produtoRepository.findById(id);

    if (produtoOptional.isEmpty()) {
        return "redirect:/"; // redireciona se não encontrar
    }

    Produto produto = produtoOptional.get();

    // Buscar imagens usando ProdutoImagemRepository
    List<ProdutoImagem> imagens = produtoImagemRepository.findByProdutoId(id);

    model.addAttribute("produto", produto);
    model.addAttribute("imagens", imagens);

    return "detalhesProduto"; // nome do seu template HTML
}

    

}