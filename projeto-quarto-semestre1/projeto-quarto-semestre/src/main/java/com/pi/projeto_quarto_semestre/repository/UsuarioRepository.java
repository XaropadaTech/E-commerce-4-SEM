package com.pi.projeto_quarto_semestre.repository;

import com.pi.projeto_quarto_semestre.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Usuario findByEmailAndSenha(String email, String senha);
    Usuario findByEmail(String email);
}