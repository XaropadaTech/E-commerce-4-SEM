package com.pi.projeto_quarto_semestre.repository;

import com.pi.projeto_quarto_semestre.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    // Método para encontrar o endereço padrão atual de um cliente
    Endereco findByClienteIdAndPadraoTrue(Long clienteId);
    Endereco findFirstByClienteIdAndTipoOrderByIdAsc(Long clienteId, Endereco.Tipo tipo);


}