package com.pi.projeto_quarto_semestre.repository;

import com.pi.projeto_quarto_semestre.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    // Para esta funcionalidade, não precisamos de métodos customizados aqui.
    // O JpaRepository já nos dá tudo (como 'saveAll') de que precisamos,
    // pois os itens serão salvos através do objeto Pedido (usando cascade).

}
