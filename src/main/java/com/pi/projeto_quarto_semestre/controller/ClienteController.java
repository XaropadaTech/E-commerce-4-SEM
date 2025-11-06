package com.pi.projeto_quarto_semestre.controller;

import com.pi.projeto_quarto_semestre.model.Cliente;
import com.pi.projeto_quarto_semestre.model.Endereco;
import com.pi.projeto_quarto_semestre.model.Endereco.Tipo;
import com.pi.projeto_quarto_semestre.service.ClienteService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.transaction.Transactional;

import com.pi.projeto_quarto_semestre.repository.*;

import java.util.Optional;

import com.pi.projeto_quarto_semestre.model.Pedido;
import com.pi.projeto_quarto_semestre.model.ItemPedido;
import com.pi.projeto_quarto_semestre.model.Produto;
import com.pi.projeto_quarto_semestre.repository.PedidoRepository;
import com.pi.projeto_quarto_semestre.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
            @RequestParam(value = "next", required = false) String next,
            Model model) {
        model.addAttribute("tab", tab);
        model.addAttribute("next", next); // ✅ preserva destino
        return "cliente-auth";
    }

    // POST: cadastro
    @PostMapping("/cadastrar")
    public String cadastrar(
            @RequestParam String nomeCompleto,
            @RequestParam String email,
            @RequestParam String cpf,
            @RequestParam String dataNascimento, // yyyy-MM-dd
            @RequestParam String genero, // MASCULINO | FEMININO | OUTRO | PREFIRO_NAO_INFORMAR
            @RequestParam String senha,
            // faturamento
            @RequestParam String fatCep,
            @RequestParam String fatLogradouro,
            @RequestParam String fatNumero,
            @RequestParam(required = false) String fatComplemento,
            @RequestParam String fatBairro,
            @RequestParam String fatCidade,
            @RequestParam String fatUf,
            // entrega 1 (obrigatória)
            @RequestParam String ent1Cep,
            @RequestParam String ent1Logradouro,
            @RequestParam String ent1Numero,
            @RequestParam(required = false) String ent1Complemento,
            @RequestParam String ent1Bairro,
            @RequestParam String ent1Cidade,
            @RequestParam String ent1Uf,
            // entrega extras (opcional, virão como arrays JS)
            @RequestParam(required = false, name = "entCep[]") String[] entCep,
            @RequestParam(required = false, name = "entLogradouro[]") String[] entLogradouro,
            @RequestParam(required = false, name = "entNumero[]") String[] entNumero,
            @RequestParam(required = false, name = "entComplemento[]") String[] entComplemento,
            @RequestParam(required = false, name = "entBairro[]") String[] entBairro,
            @RequestParam(required = false, name = "entCidade[]") String[] entCidade,
            @RequestParam(required = false, name = "entUf[]") String[] entUf) {
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

    // LOGIN (POST) — forma imperativa (evita erro de inferência de tipos no .map)
    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String senha,
            @RequestParam(required = false) String next,
            HttpSession session) {

        Optional<Cliente> clienteOpt = clienteRepository.findByEmailIgnoreCase(email);
        if (clienteOpt.isEmpty()) {
            return "redirect:/cliente/auth?tab=login&erro=" + url("Credenciais inválidas.");
        }

        Cliente c = clienteOpt.get();
        if (!passwordEncoder.matches(senha, c.getSenhaHash())) {
            return "redirect:/cliente/auth?tab=login&erro=" + url("Credenciais inválidas.");
        }

        // sucesso
        session.setAttribute("clienteId", c.getId());
        session.setAttribute("clienteEmail", c.getEmail());

        // ✅ volta para o destino (ex.: /checkout/start)
        if (next != null && !next.isBlank()) {
            return "redirect:" + next;
        }
        return "redirect:/carrinho";
    }

    // LOGOUT (GET) — opção rápida com alerta
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("clienteId");
        session.removeAttribute("clienteEmail");
        session.invalidate();
        return "redirect:/";
    }

    // helper para URL-encode das mensagens
    private String url(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    @GetMapping("/perfilCliente")
    public String perfilCliente(HttpSession session, Model model) {
        Long clienteId = (Long) session.getAttribute("clienteId");

        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login&erro=" + url("Faça login para acessar o perfil.");
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/cliente/auth?tab=login&erro=" + url("Sessão expirada. Faça login novamente.");
        }

        Cliente cliente = clienteOpt.get();
        model.addAttribute("cliente", cliente);
        return "perfilCliente"; // <---- remover a barra inicial
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

        // ---------------- Dados básicos ----------------
        cliente.setNomeCompleto(clienteForm.getNomeCompleto());
        cliente.setGenero(clienteForm.getGenero());

        // valida e atualiza a data de nascimento
        if (clienteForm.getDataNascimento() != null) {
            // impede data futura
            if (clienteForm.getDataNascimento().isAfter(java.time.LocalDate.now())) {
                return "redirect:/cliente/perfilCliente?erro=" + url("A data de nascimento não pode ser no futuro.");
            }

            // atualiza o valor
            cliente.setDataNascimento(clienteForm.getDataNascimento());
        }

        // Senha: só atualiza se foi informada
        if (clienteForm.getSenhaHash() != null && !clienteForm.getSenhaHash().isBlank()) {
            cliente.setSenhaHash(passwordEncoder.encode(clienteForm.getSenhaHash()));
        }

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

    // ===================================================================
    // MÉTODO NOVO PARA O REQUISITO 3: DEFINIR ENDEREÇO PADRÃO
    // ===================================================================
    @PostMapping("/perfil/endereco/definir-padrao/{enderecoId}")
    @Transactional // Garante que as duas atualizações (antigo e novo) ocorram juntas
    public String definirEnderecoPadrao(
            @PathVariable Long enderecoId, // Pega o ID da URL
            HttpSession session,
            RedirectAttributes ra) { // Para enviar mensagens de sucesso/erro

        // 1. Validar a sessão do usuário
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            // Usa o helper 'url' que você já tem para codificar a mensagem
            return "redirect:/cliente/auth?tab=login&erro=" + url("Sessão expirada.");
        }

        // 2. Encontrar o NOVO endereço que o usuário quer como padrão
        // Usa o EnderecoRepository que criamos
        Endereco novoPadrao = enderecoRepository.findById(enderecoId).orElse(null);

        // 3. Checagem de segurança: O endereço existe? Ele pertence a este cliente?
        if (novoPadrao == null || !novoPadrao.getCliente().getId().equals(clienteId)) {
            ra.addFlashAttribute("erro", "Endereço inválido ou não pertence a você.");
            return "redirect:/cliente/perfilCliente"; // Volta para a página de perfil
        }

        // 4. Encontrar o endereço padrão ANTIGO (se existir)
        // Usa o método customizado que criamos no EnderecoRepository
        Endereco antigoPadrao = enderecoRepository.findByClienteIdAndPadraoTrue(clienteId);

        // 5. Fazer a troca: desmarca o antigo e marca o novo
        // Só executa se o antigo padrão for diferente do novo que foi clicado
        if (antigoPadrao != null && !antigoPadrao.getId().equals(novoPadrao.getId())) {
            antigoPadrao.setPadrao(false);
            enderecoRepository.save(antigoPadrao); // Salva a alteração no antigo
        }

        // Marca o novo endereço como padrão (mesmo que ele já fosse, garante o estado)
        novoPadrao.setPadrao(true);
        enderecoRepository.save(novoPadrao); // Salva a alteração no novo

        ra.addFlashAttribute("ok", "Endereço padrão atualizado com sucesso!"); // Mensagem de sucesso
        return "redirect:/cliente/perfilCliente"; // Redireciona de volta para a pág. de perfil
    }

    @PostMapping("/perfil/endereco/remover/{enderecoId}")
    @Transactional // Garante que a deleção ocorra corretamente
    public String removerEndereco(
            @PathVariable Long enderecoId,
            HttpSession session,
            RedirectAttributes ra) {

        // 1. Validar a sessão do usuário
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login&erro=" + url("Sessão expirada.");
        }

        // 2. Encontrar o endereço que o usuário quer remover
        Endereco enderecoParaRemover = enderecoRepository.findById(enderecoId).orElse(null);

        // 3. Checagem de segurança e validação:
        // - O endereço existe?
        // - Pertence a este cliente?
        // - NÃO é o endereço padrão? (Regra de negócio: não permitir remover o padrão)
        if (enderecoParaRemover == null || !enderecoParaRemover.getCliente().getId().equals(clienteId)) {
            ra.addFlashAttribute("erro", "Endereço inválido ou não pertence a você.");
            return "redirect:/cliente/perfilCliente";
        }
        if (Boolean.TRUE.equals(enderecoParaRemover.getPadrao())) {
            ra.addFlashAttribute("erro",
                    "Não é possível remover o endereço padrão. Defina outro como padrão primeiro.");
            return "redirect:/cliente/perfilCliente";
        }

        // 4. Remover o endereço do banco de dados
        try {
            enderecoRepository.delete(enderecoParaRemover);
            // OU enderecoRepository.deleteById(enderecoId);

            ra.addFlashAttribute("ok", "Endereço removido com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao remover o endereço.");
        }

        return "redirect:/cliente/perfilCliente"; // Redireciona de volta para a pág. de perfil
    }

    @PostMapping("/checkout/finalizar")
    @Transactional
    public String finalizarPedido(
            @RequestParam("endereco_id") Long enderecoId,
            @RequestParam("forma_pagamento") String formaPagamento,
            @RequestParam("carrinho_json") String carrinhoJson, // Carrinho (do localStorage) enviado como texto JSON
            @RequestParam("valor_total") BigDecimal valorTotal,
            HttpSession session,
            RedirectAttributes ra) {

        // 1. Pegar o Cliente logado
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login&erro=" + url("Sessão expirada.");
        }
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 2. Pegar o Endereço de entrega selecionado
        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));

        // 3. Checagem de segurança (se o endereço pertence ao cliente)
        if (!endereco.getCliente().getId().equals(clienteId)) {
            ra.addFlashAttribute("erro", "Endereço de entrega inválido.");
            return "redirect:/checkout/endereco"; // (URL da sua página de escolher endereço)
        }

        try {
            // 4. Criar o objeto Pedido principal
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setEnderecoEntrega(endereco);
            pedido.setDataPedido(LocalDateTime.now());
            pedido.setFormaPagamento(formaPagamento);
            pedido.setValorTotal(valorTotal);

            // REQUISITO 1: "status de 'aguardando pagamento'"
            pedido.setStatus("AGUARDANDO_PAGAMENTO");

            // 5. Ler o Carrinho JSON e criar os Itens do Pedido
            // O JavaScript vai enviar o 'localStorage.getItem("carrinho")' aqui
            List<Map<String, Object>> cartItems = objectMapper.readValue(carrinhoJson,
                    new TypeReference<List<Map<String, Object>>>() {});

            if (cartItems.isEmpty()) {
                ra.addFlashAttribute("erro", "Seu carrinho está vazio.");
                return "redirect:/carrinho";
            }

            for (Map<String, Object> cartItem : cartItems) {
                // O 'id' no JSON do localStorage é o 'produtoId'
                Long produtoId = Long.parseLong(cartItem.get("id").toString());
                int quantidade = Integer.parseInt(cartItem.get("quantidade").toString());

                // Busca o produto real no banco para garantir o preço correto
                Produto produto = produtoRepository.findById(produtoId)
                        .orElseThrow(() -> new RuntimeException("Produto ID " + produtoId + " não encontrado no estoque."));

                // Cria o ItemPedido
                ItemPedido item = new ItemPedido();
                item.setProduto(produto);
                item.setQuantidade(quantidade);
                item.setPrecoUnitario(produto.getPreco()); // Guarda o preço do momento da compra

                // Adiciona o item ao pedido (o método helper no Pedido.java cuida da relação)
                pedido.adicionarItem(item);
            }

            // 6. Salvar o Pedido (e os Itens, graças ao CascadeType.ALL)
            // REQUISITO 2: "Um número sequencial do pedido deve ser gerado"
            // O 'save' faz isso e o ID é gerado automaticamente.
            Pedido pedidoSalvo = pedidoRepository.save(pedido);

            // REQUISITO 3: "Um aviso na tela..."
            // Vamos redirecionar para uma página de sucesso enviando os dados.
            ra.addFlashAttribute("sucesso", "Pedido gravado com sucesso!");
            ra.addFlashAttribute("pedidoId", pedidoSalvo.getId());
            ra.addFlashAttribute("pedidoValor", pedidoSalvo.getValorTotal());

            // Este é um novo HTML que precisamos criar (Passo 4)
            return "redirect:/cliente/pedido/sucesso";

        } catch (Exception e) {
            // Se algo der errado (ex: produto não encontrado, JSON mal formatado)
            ra.addFlashAttribute("erro", "Erro ao finalizar o pedido: " + e.getMessage());
            // Devolve o usuário para a página de pagamento (ou carrinho)
            return "redirect:/carrinho"; // (Mude para a URL da sua pág. de pagamento)
        }
    }

    @GetMapping("/pedido/sucesso")
    public String pedidoSucesso(
            Model model,
            HttpSession session,
            // O Spring automaticamente pega os "Flash Attributes"
            // que enviamos (pedidoId, pedidoValor, sucesso) e os coloca no Model.
            // Mas vamos verificar se eles existem para evitar acesso direto à URL.
            @ModelAttribute("sucesso") String sucesso) {

        // 1. Validar a sessão do usuário (segurança básica)
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) {
            return "redirect:/cliente/auth?tab=login";
        }

        // 2. Validar se o usuário veio do fluxo de checkout
        // Se ele tentar acessar /cliente/pedido/sucesso diretamente,
        // o atributo "sucesso" não existirá.
        if (sucesso == null || sucesso.isEmpty()) {
            return "redirect:/"; // Redireciona para a home
        }

        // Se ele veio do fluxo correto, os atributos (pedidoId, pedidoValor, sucesso)
        // já estão no 'model' e serão passados para o HTML.
        return "pedido-sucesso"; // Nome do nosso novo arquivo HTML
    }

}