package com.pi.projeto_quarto_semestre.repository;

import com.pi.projeto_quarto_semestre.model.ProdutoImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoImagemRepository extends JpaRepository<ProdutoImagem, Long> {

    List<ProdutoImagem> findByProdutoId(Long produtoId);

    ProdutoImagem findByProdutoIdAndImagemPadraoTrue(Long produtoId);
}
