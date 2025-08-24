-- Cria o banco de dados (se ainda não existir)
CREATE DATABASE IF NOT EXISTS quartoSemestre CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usa o banco criado
USE quartoSemestre;

-- Cria a tabela USUARIO
CREATE TABLE IF NOT EXISTS usuario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  cpf VARCHAR(14) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  status BOOLEAN NOT NULL,
  grupo VARCHAR(50) NOT NULL,
  senha VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

-- Insere dados de exemplo
INSERT INTO usuario (nome, cpf, email, status, grupo, senha) VALUES
  ('Maria Oliveira', '98765432100', 'maria@email.com', TRUE, 'estoquista', '1234'),
  ('Maria Oliveira', '98765432102', 'maria2@email.com', TRUE, 'estoquista', '1234'),
  ('Itallo', '98765432101', 'itallo@email.com', TRUE, 'administrador', '1234');