package com.pi.projeto_quarto_semestre.service;

import com.pi.projeto_quarto_semestre.model.Cliente;
import com.pi.projeto_quarto_semestre.model.Endereco;
import com.pi.projeto_quarto_semestre.repository.ClienteRepository;
import com.pi.projeto_quarto_semestre.integration.ViaCepClient;
import com.pi.projeto_quarto_semestre.util.CpfUtils;
import com.pi.projeto_quarto_semestre.util.NomeUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder; // usa o que JÁ existe
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
        // 1) normalizar e validar CPF
        String cpf = CpfUtils.somenteDigitos(cliente.getCpf());
        if (!CpfUtils.isValido(cpf)) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        if (clienteRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("Já existe um cliente com esse CPF.");
        }
        cliente.setCpf(cpf);

        // 2) validar e-mail único
        if (clienteRepository.existsByEmailIgnoreCase(cliente.getEmail())) {
            throw new IllegalArgumentException("Já existe um cliente com esse e-mail.");
        }

        // 3) validar nome
        if (!NomeUtils.nomeValido(cliente.getNomeCompleto())) {
            throw new IllegalArgumentException("Nome deve ter pelo menos duas palavras, com 3+ letras cada.");
        }

        // 4) validar endereços:
        //    - deve haver 1 FATURAMENTO obrigatório completo
        //    - deve haver pelo menos 1 ENTREGA (pode ser cópia)
        boolean temFaturamento = false;
        boolean temEntrega = false;

        for (Endereco e : cliente.getEnderecos()) {
            // CEP deve ser válido via ViaCEP
            String cep = e.getCep().replaceAll("\\D", "");
            if (cep.length() != 8 || !viaCepClient.cepValido(cep)) {
                throw new IllegalArgumentException("CEP inválido para o endereço: " + e.getTipo());
            }
            e.setCep(cep);

            if (e.getLogradouro() == null || e.getLogradouro().isBlank()
             || e.getNumero() == null || e.getNumero().isBlank()
             || e.getBairro() == null || e.getBairro().isBlank()
             || e.getCidade() == null || e.getCidade().isBlank()
             || e.getUf() == null || e.getUf().isBlank()) {
                throw new IllegalArgumentException("Endereço incompleto (" + e.getTipo() + ").");
            }

            switch (e.getTipo()) {
                case FATURAMENTO -> temFaturamento = true;
                case ENTREGA -> temEntrega = true;
            }
        }

        if (!temFaturamento) throw new IllegalArgumentException("Endereço de faturamento é obrigatório.");
        if (!temEntrega) throw new IllegalArgumentException("Pelo menos um endereço de entrega é obrigatório.");

        // 5) encriptar senha com o PasswordEncoder JÁ configurado
        cliente.setSenhaHash(passwordEncoder.encode(cliente.getSenhaHash()));

        // 6) persistir (cascade salva endereços)
        return clienteRepository.save(cliente);
    }
}
