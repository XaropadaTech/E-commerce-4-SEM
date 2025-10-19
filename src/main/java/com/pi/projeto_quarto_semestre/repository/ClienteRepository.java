package com.pi.projeto_quarto_semestre.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pi.projeto_quarto_semestre.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByCpf(String cpf);
    Optional<Cliente> findByEmailIgnoreCase(String email);
}
