package com.pi.projeto_quarto_semestre.bootstrap;

import com.pi.projeto_quarto_semestre.model.Usuario;
import com.pi.projeto_quarto_semestre.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Component
public class PasswordMigration implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public PasswordMigration(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Usuario> usuarios = repo.findAll();
        int count = 0;
        for (Usuario u : usuarios) {
            String s = u.getSenha();
            if (s != null && !s.startsWith("$2a$")) { // não está em BCrypt ainda
                u.setSenha(encoder.encode(s));
                repo.save(u);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("PasswordMigration: convertidas " + count + " senhas para BCrypt.");
        }
    }
}
