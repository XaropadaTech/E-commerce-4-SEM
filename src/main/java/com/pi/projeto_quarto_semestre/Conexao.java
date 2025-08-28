package com.pi.projeto_quarto_semestre;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
     private static final String URL = "jdbc:mysql://localhost:3306/quartoSemestre";
    private static final String USUARIO = "root";
    private static final String SENHA = "15082004";
 
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    
}
