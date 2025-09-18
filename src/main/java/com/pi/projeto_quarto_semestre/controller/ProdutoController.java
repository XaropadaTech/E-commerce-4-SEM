package com.pi.projeto_quarto_semestre.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.pi.projeto_quarto_semestre.model.Produto;
import com.pi.projeto_quarto_semestre.model.ProdutoImagem;
import com.pi.projeto_quarto_semestre.repository.ProdutoImagemRepository;
import com.pi.projeto_quarto_semestre.repository.ProdutoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoImagemRepository produtoImagemRepository;

    @GetMapping("/produtos")
    public String redirectProdutos() {
        return "redirect:/listaProdutos";
    }

    @GetMapping("/listaProdutos")
    public String listarProdutos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String nome,
            HttpSession session,
            Model model) {

        if (page < 0) page = 0;

        String grupo = (String) session.getAttribute("grupoUsuario");
        if (grupo == null) {
            return "redirect:/login";
        }
        model.addAttribute("grupo", grupo);

        Page<Produto> produtosPage = produtoRepository.findByNomeContainingIgnoreCaseOrderByIdDesc(
                nome, PageRequest.of(page, 10));

        int totalPaginas = produtosPage.getTotalPages();
        if (page >= totalPaginas && totalPaginas > 0) {
            page = totalPaginas - 1;
            produtosPage = produtoRepository.findByNomeContainingIgnoreCaseOrderByIdDesc(
                    nome, PageRequest.of(page, 10));
        }

        model.addAttribute("produtos", produtosPage.getContent());
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("filtroNome", nome);

        return "listaProdutos";
    }

    @GetMapping("/produtos/form")
public String abrirFormulario(@RequestParam(required = false) Long id,
                              Model model,
                              HttpSession session) {
    String grupo = (String) session.getAttribute("grupoUsuario");
    if (grupo == null) {
        return "redirect:/login";
    }

    boolean somenteEstoque = "ESTOQUISTA".equalsIgnoreCase(grupo);

    // Estoquista não pode abrir form sem ID (bloqueia criação)
    if (somenteEstoque && id == null) {
        return "redirect:/listaProdutos";
    }

    Produto produto = new Produto();
    if (id != null) {
        produto = produtoRepository.findById(id).orElse(new Produto());
    }

    // Se já existir produto, carregar imagens
    if (produto.getId() != null) {
        List<ProdutoImagem> imagens = produtoImagemRepository.findByProdutoId(produto.getId());
        produto.setImagens(imagens);
    }

    model.addAttribute("grupo", grupo);
    model.addAttribute("somenteEstoque", somenteEstoque);
    model.addAttribute("produto", produto);

    return "formProduto";
}


   @PostMapping("/produtos/salvar")
public String salvarProduto(
        @RequestParam(value = "id", required = false) Long id,
        @RequestParam(value = "nome", required = false) String nome,                 // <- agora opcional
        @RequestParam(value = "avaliacao", required = false) Double avaliacao,
        @RequestParam(value = "descricao", required = false) String descricao,       // <- agora opcional
        @RequestParam(value = "preco", required = false) Double preco,               // <- agora opcional
        @RequestParam("qtdEstoque") Integer qtdEstoque,                              // <- estoquista envia só isso (além do id)
        @RequestParam(value = "status", required = false) Boolean status,
        @RequestParam(value = "imagens", required = false) MultipartFile[] imagens,
        @RequestParam(value = "imagemPrincipal", required = false) String imagemPrincipal,
        HttpSession session
) throws IOException {

    String grupo = (String) session.getAttribute("grupoUsuario");
    if (grupo == null) {
        return "redirect:/login";
    }

    boolean somenteEstoque = "ESTOQUISTA".equalsIgnoreCase(grupo);

    // --- FLUXO RESTRITO: ESTOQUISTA SÓ PODE ATUALIZAR QUANTIDADE ---
    if (somenteEstoque) {
        if (id == null) {
            // Estoquista não cria produto
            return "redirect:/listaProdutos";
        }

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (qtdEstoque == null || qtdEstoque < 0) {
            // Regra simples de validação
            return "redirect:/produtos/form?id=" + id;
        }

        produto.setQtdEstoque(qtdEstoque);
        produtoRepository.save(produto); // reflete no banco (H15)
        return "redirect:/listaProdutos";
    }

    // --- FLUXO NORMAL (ADMIN/OUTROS GRUPOS) ---
    // Aqui os campos que ficaram "required=false" no RequestParam
    // são verificados manualmente para evitar 400 quando o form tiver campos desabilitados em cenários específicos.
    if (nome == null || nome.isBlank()) {
        throw new RuntimeException("Nome é obrigatório.");
    }
    if (descricao == null) {
        descricao = ""; // se quiser permitir vazio
    }
    if (preco == null || preco <= 0) {
        throw new RuntimeException("Preço inválido! O preço deve ser maior que zero.");
    }

    Produto produtoSalvo;
    if (id != null) {
        produtoSalvo = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    } else {
        produtoSalvo = new Produto();
    }

    produtoSalvo.setNome(nome);

    if (avaliacao != null && avaliacao >= 1.0 && avaliacao <= 5.0) {
        produtoSalvo.setAvaliacao(BigDecimal.valueOf(avaliacao));
    } else {
        // default
        produtoSalvo.setAvaliacao(BigDecimal.valueOf(3.0));
    }

    produtoSalvo.setDescricao(descricao);
    produtoSalvo.setPreco(BigDecimal.valueOf(preco));
    produtoSalvo.setQtdEstoque(qtdEstoque);
    produtoSalvo.setStatus(status != null ? status : true);

    produtoSalvo = produtoRepository.save(produtoSalvo);

    // Salvar novas imagens (somente para não-estoquista)
    List<ProdutoImagem> imagensSalvas = new ArrayList<>();
    if (imagens != null && imagens.length > 0) {
        for (MultipartFile arquivo : imagens) {
            if (!arquivo.isEmpty()) {
                String novoNome = salvarArquivoNoDiretorio(arquivo);

                ProdutoImagem imagem = new ProdutoImagem();
                imagem.setProduto(produtoSalvo);
                imagem.setCaminhoImagem("/imagens/" + novoNome);
                imagem.setNomeOriginal(arquivo.getOriginalFilename());
                imagem.setImagemPadrao(false);

                imagensSalvas.add(produtoImagemRepository.save(imagem));
            }
        }
    }

    // Atualizar status da imagem principal (se informado)
    if (imagemPrincipal != null) {
        List<ProdutoImagem> todasImagens = produtoImagemRepository.findByProdutoId(produtoSalvo.getId());

        for (ProdutoImagem img : todasImagens) {
            if (imagemPrincipal.startsWith("nova-")) {
                // Ex.: nova-0, nova-1 ...
                int indice = Integer.parseInt(imagemPrincipal.substring(5));
                boolean principalNova = (imagensSalvas != null
                        && indice >= 0
                        && indice < imagensSalvas.size()
                        && imagensSalvas.get(indice).getId().equals(img.getId()));
                img.setImagemPadrao(principalNova);
            } else {
                boolean isPrincipal = img.getId().toString().equals(imagemPrincipal);
                img.setImagemPadrao(isPrincipal);
            }
            produtoImagemRepository.save(img);
        }
    }

    return "redirect:/listaProdutos";
}



    public String salvarArquivoNoDiretorio(MultipartFile arquivo) throws IOException {
    // pega a pasta onde o projeto está rodando + "/imagens_upload"
    String pastaImagens = System.getProperty("user.dir") + "/imagens_upload";

    File dir = new File(pastaImagens);
    if (!dir.exists()) {
        boolean criado = dir.mkdirs();
        if (!criado) {
            throw new IOException("Não foi possível criar o diretório: " + pastaImagens);
        }
    }

    String extensao = FilenameUtils.getExtension(arquivo.getOriginalFilename());
    String novoNome = UUID.randomUUID().toString() + "." + extensao;

    File destino = new File(dir, novoNome);
    arquivo.transferTo(destino);

    return novoNome;
    }

    @GetMapping("/produtos/visualizar")
    public String visualizarProduto(@RequestParam Long id, Model model, HttpSession session) {
        String grupo = (String) session.getAttribute("grupoUsuario");
        if (grupo == null) {
            return "redirect:/login";
        }

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Carregar imagens do produto
        List<ProdutoImagem> imagens = produtoImagemRepository.findByProdutoId(produto.getId());
        produto.setImagens(imagens);

        model.addAttribute("produto", produto);
        model.addAttribute("grupo", grupo);

        return "visualizarProduto";
    }

    @GetMapping("/produtos/excluirImagem")
    public String excluirImagem(@RequestParam Long imagemId, HttpSession session) {
        String grupo = (String) session.getAttribute("grupoUsuario");
        if (grupo == null) {
            return "redirect:/login";
        }

        // Apenas administradores podem excluir imagens
        if (!"administrador".equalsIgnoreCase(grupo)) {
            return "redirect:/listaProdutos";
        }

        ProdutoImagem imagem = produtoImagemRepository.findById(imagemId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));

        Long produtoId = imagem.getProduto().getId();

        // Excluir arquivo físico
        try {
            String caminhoCompleto = System.getProperty("user.dir") + "/imagens_upload" + 
                                   imagem.getCaminhoImagem().replace("/imagens", "");
            File arquivo = new File(caminhoCompleto);
            if (arquivo.exists()) {
                arquivo.delete();
            }
        } catch (Exception e) {
            // Log do erro, mas continua a exclusão do banco
            System.err.println("Erro ao excluir arquivo físico: " + e.getMessage());
        }

        // Excluir do banco
        produtoImagemRepository.delete(imagem);

        return "redirect:/produtos/form?id=" + produtoId;
    }

    @PostMapping("/produtos/{id}/alternar-status")
    public String alternarStatusProdutoPost(@PathVariable Long id) {
    Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

    produto.setStatus(!produto.getStatus());
    produtoRepository.save(produto);

    return "redirect:/listaProdutos";
}
}