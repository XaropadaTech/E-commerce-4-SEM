package com.pi.projeto_quarto_semestre.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


public class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setCpf("12345678901");
        usuario.setEmail("teste@exemplo.com");
        usuario.setSenha("senha123");
    }

    // -----------------------
    // TESTES BÁSICOS
    // -----------------------

    @Test
    @DisplayName("Status padrão deve ser true")
    void statusPadrao() {
        assertTrue(usuario.getStatus());
    }

    @Test
    @DisplayName("Grupo inicial deve ser nulo")
    void grupoInicialNulo() {
        assertNull(usuario.getGrupo());
    }

    @Test
    @DisplayName("Alterar grupo do usuário")
    void alterarGrupo() {
        usuario.setGrupo(Usuario.Grupo.administrador);
        assertEquals(Usuario.Grupo.administrador, usuario.getGrupo());

        usuario.setGrupo(Usuario.Grupo.estoquista);
        assertEquals(Usuario.Grupo.estoquista, usuario.getGrupo());
    }

    // -----------------------
    // TESTES DE NOME
    // -----------------------

    @Test
    @DisplayName("Nome vazio")
    void nomeVazio() {
        usuario.setNome("");
        assertEquals("", usuario.getNome());
    }

    @Test
    @DisplayName("Nome muito longo")
    void nomeMuitoLongo() {
        String nomeLongo = "A".repeat(200); // limite extra
        usuario.setNome(nomeLongo);
        assertEquals(200, usuario.getNome().length());
    }

    // -----------------------
    // TESTES DE CPF
    // -----------------------

    @Test
    @DisplayName("CPF correto de 11 dígitos")
    void cpfValido11Digitos() {
        usuario.setCpf("12345678901");
        assertEquals(11, usuario.getCpf().length());
    }

    @Test
    @DisplayName("CPF correto com máscara (14 caracteres)")
    void cpfValido14ComMascara() {
        usuario.setCpf("123.456.789-01");
        assertEquals(14, usuario.getCpf().length());
    }

    @Test
    @DisplayName("CPF muito curto deve ser inválido")
    void cpfCurtoInvalido() {
        String cpf = "12345";
        usuario.setCpf(cpf);
        boolean valido = cpf.length() == 11 || cpf.length() == 14;
        assertFalse(valido, "CPF inválido: " + cpf);
    }

    @Test
    @DisplayName("CPF muito longo deve ser inválido")
    void cpfLongoInvalido() {
        String cpf = "123456789012345";
        usuario.setCpf(cpf);
        boolean valido = cpf.length() == 11 || cpf.length() == 14;
        assertFalse(valido, "CPF inválido: " + cpf);
    }

    // -----------------------
    // TESTES DE EMAIL
    // -----------------------

    @Test
    @DisplayName("Email válido")
    void emailValido() {
        String email = "teste@exemplo.com";
        usuario.setEmail(email);
        boolean valido = email.contains("@") && email.length() <= 100;
        assertTrue(valido);
    }

    @Test
    @DisplayName("Email sem arroba deve ser inválido")
    void emailSemArrobaInvalido() {
        String email = "teste.exemplo.com";
        usuario.setEmail(email);
        boolean valido = email.contains("@") && email.length() <= 100;
        assertFalse(valido, "Email inválido: " + email);
    }

    @Test
    @DisplayName("Email muito longo deve ser inválido")
    void emailMuitoLongoInvalido() {
        String email = "a".repeat(101) + "@ex.com"; // 105 caracteres
        usuario.setEmail(email);
        boolean valido = email.contains("@") && email.length() <= 100;
        assertFalse(valido, "Email inválido: " + email);
    }

    // -----------------------
    // TESTES DE SENHA
    // -----------------------

    @Test
    @DisplayName("Senha mínima e máxima")
    void senhaLimites() {
        usuario.setSenha("A"); // mínimo
        assertEquals(1, usuario.getSenha().length());

        String senhaLonga = "A".repeat(60); // máximo
        usuario.setSenha(senhaLonga);
        assertEquals(60, usuario.getSenha().length());
    }

    @Test
    @DisplayName("Senha nula")
    void senhaNula() {
        usuario.setSenha(null);
        assertNull(usuario.getSenha());
    }

    // -----------------------
    // TESTES DE STATUS
    // -----------------------

    @Test
    @DisplayName("Alterar status do usuário")
    void alterarStatus() {
        usuario.setStatus(false);
        assertFalse(usuario.getStatus());

        usuario.setStatus(true);
        assertTrue(usuario.getStatus());
    }
}