package com.pi.projeto_quarto_semestre.repository;

import com.pi.projeto_quarto_semestre.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmailAndSenha(String email, String senha);
    Usuario findByEmail(String email);

    List<Usuario> findByNomeContainingIgnoreCase(String nome);
}