package com.pi.projeto_quarto_semestre.repository;

import com.pi.projeto_quarto_semestre.model.Produto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Produto> findAllByOrderByCriadoEmDesc(Pageable pageable);
    Page<Produto> findByNomeContainingIgnoreCaseOrderByIdDesc(String nome, Pageable pageable);

    List<Produto> findByStatusTrue();
    Page<Produto> findByStatusTrueOrderByCriadoEmDesc(Pageable pageable);
    Page<Produto> findByNomeContainingIgnoreCaseAndStatusTrueOrderByIdDesc(String nome, Pageable pageable);



}