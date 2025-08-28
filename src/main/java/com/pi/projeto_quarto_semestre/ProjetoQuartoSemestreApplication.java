package com.pi.projeto_quarto_semestre;

import java.sql.Connection;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjetoQuartoSemestreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoQuartoSemestreApplication.class, args);

		// Teste de Conexão com o banco
		  try (Connection conexao = Conexao.conectar()) {
            System.out.println("Conectado com sucesso ao banco!");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
	}

}
