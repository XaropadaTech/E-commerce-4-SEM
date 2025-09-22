package com.pi.projeto_quarto_semestre.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

        // validação de quantidade (não negativa)
        if (qtdEstoque == null || qtdEstoque < 0) {
            return "redirect:/produtos/form?id=" + id;
        }

        produto.setQtdEstoque(qtdEstoque);
        produtoRepository.save(produto); // reflete no banco
        return "redirect:/listaProdutos";
    }

    // --- FLUXO NORMAL (ADMIN/OUTROS GRUPOS) ---

    // Normalizações de entrada
    nome = (nome == null) ? null : nome.trim();
    descricao = (descricao == null) ? "" : descricao;

    // Validações server-side (sem anotações), compatíveis com limites do BD
    // nome: obrigatório e <= 200
    if (nome == null || nome.isEmpty()) {
        throw new RuntimeException("Nome é obrigatório.");
    }
    if (nome.length() > 200) {
        throw new RuntimeException("Nome deve ter no máximo 200 caracteres.");
    }

    // avaliação: obrigatória, 1.0..5.0 e passo 0.5
    if (avaliacao == null) {
        throw new RuntimeException("Avaliação é obrigatória.");
    } else {
        BigDecimal a = BigDecimal.valueOf(avaliacao).setScale(1, RoundingMode.HALF_UP);
        boolean faixa = a.compareTo(new BigDecimal("1.0")) >= 0
                && a.compareTo(new BigDecimal("5.0")) <= 0;
        boolean passo = a.remainder(new BigDecimal("0.5")).compareTo(BigDecimal.ZERO) == 0;
        if (!faixa || !passo) {
            throw new RuntimeException("Avaliação deve estar entre 1,0 e 5,0 em passos de 0,5.");
        }
    }

    // descricao: <= 2000
    if (descricao.length() > 2000) {
        throw new RuntimeException("Descrição deve ter no máximo 2000 caracteres.");
    }

    // preco: obrigatório, >= 0, no máximo 2 casas
    if (preco == null) {
        throw new RuntimeException("Preço é obrigatório.");
    }
    BigDecimal precoBD = BigDecimal.valueOf(preco);
    if (precoBD.compareTo(BigDecimal.ZERO) < 0) {
        throw new RuntimeException("Preço não pode ser negativo.");
    }
    try {
        // Verifica estritamente “no máximo 2 casas”
        precoBD.setScale(2, RoundingMode.UNNECESSARY);
    } catch (ArithmeticException ex) {
        throw new RuntimeException("Preço deve ter no máximo 2 casas decimais.");
    }
    // Padroniza escala para gravar
    precoBD = precoBD.setScale(2, RoundingMode.HALF_UP);

    // qtdEstoque: obrigatória e >= 0
    if (qtdEstoque == null || qtdEstoque < 0) {
        throw new RuntimeException("Quantidade em estoque deve ser um inteiro maior ou igual a zero.");
    }

    Produto produtoSalvo;
    if (id != null) {
        produtoSalvo = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    } else {
        produtoSalvo = new Produto();
    }

    // atribuições já normalizadas
    produtoSalvo.setNome(nome);
    produtoSalvo.setDescricao(descricao);
    produtoSalvo.setPreco(precoBD);
    produtoSalvo.setQtdEstoque(qtdEstoque);
    produtoSalvo.setStatus(status != null ? status : true);

    // avaliacao normalizada e validada (1 casa)
    BigDecimal avaliacaoBD = BigDecimal.valueOf(avaliacao).setScale(1, RoundingMode.HALF_UP);
    produtoSalvo.setAvaliacao(avaliacaoBD);

    try {
        // tratamento de violação do BD (limites/escala/precisão)
        produtoSalvo = produtoRepository.save(produtoSalvo);
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
        throw new RuntimeException(
            "Um ou mais campos excedem os limites do banco (nome ≤ 200, descrição ≤ 2000, preço com 2 casas). Ajuste e tente novamente.",
            ex
        );
    }

    // Salvar novas imagens (somente para não-estoquista)
    List<ProdutoImagem> imagensSalvas = new ArrayList<>();
    if (imagens != null && imagens.length > 0) {
        for (MultipartFile arquivo : imagens) {
            if (!arquivo.isEmpty()) {
                String novoNome = salvarArquivoNoDiretorio(arquivo); // sua rotina já existente

                ProdutoImagem imagem = new ProdutoImagem();
                imagem.setProduto(produtoSalvo);
                imagem.setCaminhoImagem("/imagens/" + novoNome); // URL pública salva no banco
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
    } else {
        // garante 1 principal se não houver
        List<ProdutoImagem> todasImagens = produtoImagemRepository.findByProdutoId(produtoSalvo.getId());
        if (!todasImagens.isEmpty() && todasImagens.stream().noneMatch(ProdutoImagem::getImagemPadrao)) {
            ProdutoImagem primeira = todasImagens.get(0);
            primeira.setImagemPadrao(true);
            produtoImagemRepository.save(primeira);
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