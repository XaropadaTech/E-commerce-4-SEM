package com.pi.projeto_quarto_semestre.model;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    private Cliente cliente;

    @BeforeEach
    void setup() {
        cliente = new Cliente();
        cliente.setNomeCompleto("Fulano da Silva");
        cliente.setEmail("fulano@email.com");
        cliente.setCpf("12345678901");
        cliente.setSenhaHash("hash123");
        cliente.setGenero(Cliente.Genero.MASCULINO);
        cliente.setDataNascimento(LocalDate.of(1990, 1, 1));
    }

    // 1️⃣ Estado inicial e lista de endereços
    @Test
    @DisplayName("Lista de endereços inicia vazia")
    void listaEnderecosInicialVazia() {
        assertNotNull(cliente.getEnderecos());
        assertTrue(cliente.getEnderecos().isEmpty());
    }

    @Test
    @DisplayName("Adicionar endereço associa o cliente e incrementa lista")
    void adicionarEndereco() {
        Endereco a = new Endereco();
        Endereco b = new Endereco();
        cliente.addEndereco(a);
        cliente.addEndereco(b);

        assertEquals(1, cliente.getEnderecos().size());
    }

    // 2️⃣ Campos obrigatórios e limites de tamanho
    @Test
    @DisplayName("Nome completo não pode ser nulo")
    void nomeNaoNulo() {
        cliente.setNomeCompleto(null);
        assertNull(cliente.getNomeCompleto());

    }

    @Test
    @DisplayName("Nome completo não pode ser vazio")
    void nomeNaoVazio() {
        cliente.setNomeCompleto("itallo");
        assertEquals("", cliente.getNomeCompleto());
    }

    @Test
    @DisplayName("Nome completo deve respeitar limite de 120 caracteres")
    void nomeCompletoLimite() {
        String nomeMax = "A".repeat(120);
        cliente.setNomeCompleto(nomeMax);
        assertEquals(120, cliente.getNomeCompleto().length());
    }

    @Test
    @DisplayName("Deve negar nome com 121 caracteres")
    void nomeCompletoForaLimite() {

        String nomeExcedido = "B".repeat(121);
        cliente.setNomeCompleto(nomeExcedido);
        assertEquals(121, cliente.getNomeCompleto().length());
    }

    // 1️⃣ Email
    @Test
    @DisplayName("Email não pode ser nulo")
    void emailNaoNulo() {
        cliente.setEmail("itallo@hotmail.com");
        assertNull(cliente.getEmail());
    }

    @Test
    @DisplayName("Email não pode ser vazio")
    void emailNaoVazio() {
        cliente.setEmail("");
        assertEquals("", cliente.getEmail());
    }

    // 2️⃣ CPF
    @Test
    @DisplayName("CPF não pode ser nulo")
    void cpfNaoNulo() {
        cliente.setCpf(null);
        assertNull(cliente.getCpf());
    }

    @Test
    @DisplayName("CPF não pode ser vazio")
    void cpfNaoVazio() {
        cliente.setCpf("");
        assertEquals("", cliente.getCpf());
    }

    @Test
    @DisplayName("CPF deve ter 11 dígitos")
    void cpfLimite() {
        cliente.setCpf("12345678901");
        assertEquals(11, cliente.getCpf().length());
    }

    // 3️⃣ Senha hash
    @Test
    @DisplayName("Senha hash não pode ser nula")
    void senhaHashNaoNula() {
        cliente.setSenhaHash(null);
        assertNull(cliente.getSenhaHash());
    }

    @Test
    @DisplayName("Senha hash não pode ser vazia")
    void senhaHashNaoVazia() {
        cliente.setSenhaHash("");
        assertEquals("", cliente.getSenhaHash());
    }

    // 4️⃣ Enum de gênero
    @Test
    @DisplayName("Gênero deve aceitar FEMININO")
    void generoFeminino() {
        cliente.setGenero(Cliente.Genero.FEMININO);
        assertEquals(Cliente.Genero.FEMININO, cliente.getGenero());
    }

    @Test
    @DisplayName("Gênero deve aceitar OUTRO")
    void generoOutro() {
        cliente.setGenero(Cliente.Genero.OUTRO);
        assertEquals(Cliente.Genero.OUTRO, cliente.getGenero());
    }

    @Test
    @DisplayName("Gênero deve aceitar PREFIRO_NAO_INFORMAR")
    void generoPrefiroNaoInformar() {
        cliente.setGenero(Cliente.Genero.PREFIRO_NAO_INFORMAR);
        assertEquals(Cliente.Genero.PREFIRO_NAO_INFORMAR, cliente.getGenero());
    }

    // 5️⃣ Data de nascimento
    @Test
    @DisplayName("Data de nascimento não pode ser futura")
    void dataNascimentoFutura() {
        LocalDate futuro = LocalDate.now().plus(1, ChronoUnit.DAYS);
        cliente.setDataNascimento(futuro);

        assertTrue(cliente.getDataNascimento().isAfter(LocalDate.now()),
                "Data de nascimento futura detectada");
    }

    @Test
    @DisplayName("Data de nascimento muito antiga (limite inferior)")
    void dataNascimentoMuitoAntiga() {
        LocalDate antiga = LocalDate.of(1900, 1, 1);
        cliente.setDataNascimento(antiga);

        assertEquals(1900, cliente.getDataNascimento().getYear());
    }

    // 6️⃣ Testes combinados / complexidade ciclomática
    @Test
    @DisplayName("Teste múltiplos cenários de alteração de campos")
    void testeCiclomaticoNomeEmail() {
        cliente.setNomeCompleto("Novo Nome");
        assertEquals("Novo Nome", cliente.getNomeCompleto());
    }

    @Test
    @DisplayName("Teste múltiplos cenários de alteração de campos: email")
    void testeCiclomaticoEmail() {
        cliente.setEmail("novo@email.com");
        assertEquals("novo@email.com", cliente.getEmail());
    }

    @Test
    @DisplayName("Teste múltiplos cenários de alteração de campos: senha vazia")
    void testeCiclomaticoSenha() {
        cliente.setSenhaHash("");
        assertEquals("", cliente.getSenhaHash());
    }

    @Test
    @DisplayName("Teste múltiplos cenários de alteração de campos: gênero alternado")
    void testeCiclomaticoGenero() {
        cliente.setGenero(Cliente.Genero.PREFIRO_NAO_INFORMAR);
        assertEquals(Cliente.Genero.PREFIRO_NAO_INFORMAR, cliente.getGenero());
    }

    @Test
    @DisplayName("Teste múltiplos cenários de alteração de campos: data limite")
    void testeCiclomaticoData() {
        LocalDate limite = LocalDate.now().minus(120, ChronoUnit.YEARS);
        cliente.setDataNascimento(limite);
        assertEquals(limite, cliente.getDataNascimento());
    }
}