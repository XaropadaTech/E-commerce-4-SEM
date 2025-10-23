package com.pi.projeto_quarto_semestre.service;

import com.pi.projeto_quarto_semestre.integration.ViaCepClient;
import com.pi.projeto_quarto_semestre.model.Cliente;
import com.pi.projeto_quarto_semestre.model.Endereco;
import com.pi.projeto_quarto_semestre.repository.ClienteRepository;
import com.pi.projeto_quarto_semestre.util.CpfUtils;
import com.pi.projeto_quarto_semestre.util.NomeUtils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ViaCepClient viaCepClient;

    public ClienteService(ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder,
            ViaCepClient viaCepClient) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.viaCepClient = viaCepClient;
    }

    @Transactional
    public Cliente cadastrarNovo(Cliente cliente) {
        // 1) Normalizar e validar CPF
        String cpf = CpfUtils.somenteDigitos(cliente.getCpf());
        if (!CpfUtils.isValido(cpf)) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        if (clienteRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("Já existe um cliente com esse CPF.");
        }
        cliente.setCpf(cpf);

        // 2) Validar e-mail único
        if (clienteRepository.existsByEmailIgnoreCase(cliente.getEmail())) {
            throw new IllegalArgumentException("Já existe um cliente com esse e-mail.");
        }

        // 3) Validar nome completo
        if (!NomeUtils.nomeValido(cliente.getNomeCompleto())) {
            throw new IllegalArgumentException("Nome deve conter ao menos duas palavras com 3+ letras cada.");
        }

        // 4) Validar endereços
        boolean temFaturamento = false;
        boolean temEntrega = false;

        for (Endereco endereco : cliente.getEnderecos()) {
            // Normalizar CEP (apenas números)
            String cep = endereco.getCep().replaceAll("\\D", "");
            endereco.setCep(cep);

            boolean cepEhValido = cep.length() == 8 && viaCepClient.cepValido(cep);
            System.out.println("Validando CEP: " + cep + " -> " + cepEhValido);
            if (!cepEhValido) {
                throw new IllegalArgumentException("CEP inválido no endereço de " + endereco.getTipo());
            }

            // Validar campos obrigatórios do endereço
            if (isVazio(endereco.getLogradouro()) ||
                    isVazio(endereco.getNumero()) ||
                    isVazio(endereco.getBairro()) ||
                    isVazio(endereco.getCidade()) ||
                    isVazio(endereco.getUf())) {
                throw new IllegalArgumentException("Endereço incompleto (" + endereco.getTipo() + ").");
            }

            // Verificar tipo de endereço
            switch (endereco.getTipo()) {
                case FATURAMENTO -> temFaturamento = true;
                case ENTREGA -> temEntrega = true;
            }
        }

        if (!temFaturamento) {
            throw new IllegalArgumentException("É obrigatório um endereço de FATURAMENTO.");
        }
        if (!temEntrega) {
            throw new IllegalArgumentException("Pelo menos um endereço de ENTREGA é obrigatório.");
        }

        // 5) Encriptar senha
        cliente.setSenhaHash(passwordEncoder.encode(cliente.getSenhaHash()));

        // 6) Persistir cliente (endereços devem ser salvos via cascade)
        return clienteRepository.save(cliente);
    }

    private boolean isVazio(String valor) {
        return valor == null || valor.isBlank();
    }
}