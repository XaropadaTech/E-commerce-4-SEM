package com.pi.projeto_quarto_semestre.repository;

import com.pi.projeto_quarto_semestre.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Método para a futura tela "Meus Pedidos"
    // Encontra todos os pedidos de um cliente específico, ordenados do mais novo para o mais antigo
    List<Pedido> findByClienteIdOrderByDataPedidoDesc(Long clienteId);
    Optional<Pedido> findByIdAndClienteId(Long id, Long clienteId);

}
