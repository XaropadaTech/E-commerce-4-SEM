package com.pi.projeto_quarto_semestre.service;

import com.pi.projeto_quarto_semestre.integration.ViaCepClient;
import com.pi.projeto_quarto_semestre.model.Cliente;
import com.pi.projeto_quarto_semestre.model.Endereco;
import com.pi.projeto_quarto_semestre.repository.ClienteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @InjectMocks
    private ClienteService clienteService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ViaCepClient viaCepClient;

    // --- MÉTODOS AJUDANTES (Para não repetir código) ---

    private Cliente criarClienteBase() {
        Cliente c = new Cliente();
        c.setNomeCompleto("Maria Silva"); // Nome Válido
        c.setEmail("maria@teste.com");       // E-mail Válido
        c.setCpf("09799481007");             // CPF (formato válido para o mock)
        c.setSenhaHash("senha123");
        c.setEnderecos(new ArrayList<>());
        return c;
    }

    private Endereco criarEndereco(Endereco.Tipo tipo, String cep) {
        Endereco e = new Endereco();
        e.setTipo(tipo);
        e.setCep(cep);
        e.setLogradouro("Rua Teste");
        e.setNumero("100");
        e.setBairro("Centro");
        e.setCidade("São Paulo");
        e.setUf("SP");
        return e;
    }

    // --- TESTES DE SUCESSO (CAMINHO FELIZ) ---

    @Test
    @DisplayName("CT-01: Deve cadastrar com sucesso (Senha Criptografada + 2 Endereços)")
    void testeCadastroCompletoComSucesso() {
        // 1. Arrange (Cenário Perfeito)
        Cliente cliente = criarClienteBase();

        // Adiciona os DOIS tipos de endereço obrigatórios
        cliente.getEnderecos().add(criarEndereco(Endereco.Tipo.FATURAMENTO, "01001000"));
        cliente.getEnderecos().add(criarEndereco(Endereco.Tipo.ENTREGA, "02002000"));

        // Mocks (O "Sinal Verde" do sistema)
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false); // CPF é único
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false); // Email é único
        when(viaCepClient.cepValido(anyString())).thenReturn(true); // Todos CEPs são válidos
        when(passwordEncoder.encode("senha123")).thenReturn("HASH_SECRETO_XYZ"); // Simula criptografia
        when(clienteRepository.save(cliente)).thenReturn(cliente); // Simula salvar

        // 2. Act (Ação)
        Cliente resultado = clienteService.cadastrarNovo(cliente);

        // 3. Assert (Verificações)
        Assertions.assertNotNull(resultado);

        // Verifica se a senha foi criptografada (Requisito importante!)
        Assertions.assertEquals("HASH_SECRETO_XYZ", resultado.getSenhaHash());

        // Verifica se o método save foi chamado 1 vez
        verify(clienteRepository, times(1)).save(cliente);
    }

    // --- TESTES DE VALIDAÇÃO (ERROS) ---

    @Test
    @DisplayName("CT-02: Deve bloquear cadastro se faltar endereço de Faturamento")
    void testeFaltaFaturamento() {
        Cliente cliente = criarClienteBase();
        // Só adiciona entrega
        cliente.getEnderecos().add(criarEndereco(Endereco.Tipo.ENTREGA, "01001000"));

        // Mocks básicos para passar pelas primeiras validações
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(viaCepClient.cepValido(anyString())).thenReturn(true);

        // Ação e Verificação
        Exception erro = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarNovo(cliente);
        });

        Assertions.assertEquals("É obrigatório um endereço de FATURAMENTO.", erro.getMessage());
    }

    @Test
    @DisplayName("CT-03: Deve bloquear cadastro se faltar endereço de Entrega")
    void testeFaltaEntrega() {
        Cliente cliente = criarClienteBase();
        // Só adiciona faturamento
        cliente.getEnderecos().add(criarEndereco(Endereco.Tipo.FATURAMENTO, "01001000"));

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(viaCepClient.cepValido(anyString())).thenReturn(true);

        Exception erro = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarNovo(cliente);
        });

        Assertions.assertEquals("Pelo menos um endereço de ENTREGA é obrigatório.", erro.getMessage());
    }

    @Test
    @DisplayName("CT-04: Deve bloquear nome inválido (muito curto)")
    void testeNomeInvalido() {
        Cliente cliente = criarClienteBase();
        cliente.setNomeCompleto("Ana"); // Erro aqui

        // Mocks de CPF/Email ok
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        Exception erro = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarNovo(cliente);
        });

        Assertions.assertTrue(erro.getMessage().contains("Nome deve conter ao menos duas palavras"));
    }

    @Test
    @DisplayName("CT-05: Deve bloquear se o CEP for inválido na API")
    void testeCepInvalido() {
        Cliente cliente = criarClienteBase();
        cliente.getEnderecos().add(criarEndereco(Endereco.Tipo.FATURAMENTO, "00000000"));

        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        // O PULO DO GATO: API retorna false
        when(viaCepClient.cepValido("00000000")).thenReturn(false);

        Exception erro = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarNovo(cliente);
        });

        Assertions.assertTrue(erro.getMessage().contains("CEP inválido"));
    }

    @Test
    @DisplayName("CT-06: Deve bloquear cadastro com CPF matematicamente inválido")
    void testeCpfInvalido() {
        // 1. Cenário
        Cliente cliente = criarClienteBase();
        // Definimos um CPF com todos números iguais (matematicamente inválido)
        cliente.setCpf("11111111111");

        // 2. Ação e Verificação
        // Como a validação é feita pela classe utilitária estática CpfUtils logo no início,
        // esperamos o erro imediato.
        Exception erro = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarNovo(cliente);
        });

        Assertions.assertEquals("CPF inválido.", erro.getMessage());
    }

    @Test
    @DisplayName("CT-07: Deve bloquear cadastro se o E-mail já existe no banco")
    void testeEmailDuplicado() {
        // 1. Cenário
        Cliente cliente = criarClienteBase();

        // 2. Configuração dos Mocks
        // CPF é válido e não existe (para passar pela primeira barreira)
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);

        // O PULO DO GATO: O repositório diz "SIM, esse e-mail já existe!"
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        // 3. Ação e Verificação
        Exception erro = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarNovo(cliente);
        });

        Assertions.assertEquals("Já existe um cliente com esse e-mail.", erro.getMessage());
    }

    @Test
    @DisplayName("CT-08: Deve bloquear endereço com campos obrigatórios vazios (Ex: Bairro)")
    void testeEnderecoIncompleto() {
        // 1. Cenário
        Cliente cliente = criarClienteBase();

        // Adiciona um endereço de Faturamento VÁLIDO (para não falhar por falta dele)
        cliente.getEnderecos().add(criarEndereco(Endereco.Tipo.FATURAMENTO, "01001000"));

        // Cria um endereço de Entrega e "estraga" ele
        Endereco enderecoInvalido = criarEndereco(Endereco.Tipo.ENTREGA, "02002000");
        enderecoInvalido.setBairro(""); // <--- O ERRO: Bairro vazio invalida o endereço

        cliente.getEnderecos().add(enderecoInvalido);

        // 2. Configuração dos Mocks
        // Precisamos que o CPF, Email e o CEP sejam válidos para o código chegar até a checagem do Bairro
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(viaCepClient.cepValido(anyString())).thenReturn(true); // O CEP existe, mas o dado interno está vazio

        // 3. Ação e Verificação
        Exception erro = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            clienteService.cadastrarNovo(cliente);
        });

        // A mensagem no seu Service é: "Endereço incompleto (ENTREGA)."
        Assertions.assertTrue(erro.getMessage().contains("Endereço incompleto"),
                "Deveria lançar erro de endereço incompleto");
    }

}