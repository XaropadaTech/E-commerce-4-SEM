-- Cria o banco de dados (se ainda não existir)
CREATE DATABASE IF NOT EXISTS quartoSemestre;

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

-- Cria a tabela de PRODUTO
CREATE TABLE IF NOT EXISTS produto (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(200) NOT NULL,
  avaliacao DECIMAL(2,1) CHECK (avaliacao >= 1.0 AND avaliacao <= 5.0), -- aceitando casas de 0.5
  descricao TEXT, -- até 2000 caracteres (TEXT cobre isso)
  preco DECIMAL(10,2) NOT NULL,
  qtd_estoque INT NOT NULL,
  status BOOLEAN NOT NULL DEFAULT TRUE, -- true = ativo, false = inativo
  criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
  atualizado_em DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Cria a tabela de IMAGENS do produto
CREATE TABLE IF NOT EXISTS produto_imagem (
  id INT AUTO_INCREMENT PRIMARY KEY,
  produto_id INT NOT NULL,
  caminho_imagem VARCHAR(255) NOT NULL, -- nome/relativo do arquivo
  nome_original VARCHAR(255), -- se quiser guardar o nome original do upload
  imagem_padrao BOOLEAN DEFAULT FALSE, -- TRUE = imagem principal
  criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabela CLIENTE
CREATE TABLE IF NOT EXISTS cliente (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nome_completo VARCHAR(120) NOT NULL,
  email VARCHAR(120) NOT NULL,
  cpf VARCHAR(14) NOT NULL,
  data_nascimento DATE NOT NULL,
  genero VARCHAR(30) NOT NULL,
  senha_hash VARCHAR(100) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_cliente_email UNIQUE (email),
  CONSTRAINT uk_cliente_cpf UNIQUE (cpf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela ENDERECO
CREATE TABLE IF NOT EXISTS endereco (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cliente_id BIGINT NOT NULL,
  tipo VARCHAR(15) NOT NULL,         -- 'FATURAMENTO' ou 'ENTREGA'
  cep VARCHAR(8) NOT NULL,           -- apenas dígitos
  logradouro VARCHAR(120) NOT NULL,
  numero VARCHAR(10) NOT NULL,
  complemento VARCHAR(60) NULL,
  bairro VARCHAR(60) NOT NULL,
  cidade VARCHAR(60) NOT NULL,
  uf CHAR(2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_endereco_cliente (cliente_id),
  CONSTRAINT fk_endereco_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
);



-- Insere dados de exemplo
INSERT INTO usuario (nome, cpf, email, status, grupo, senha) VALUES
  ('Maria Oliveira', '98765432100', 'maria@email.com', TRUE, 'estoquista', '1234'),
  ('Maria Oliveira', '98765432102', 'maria2@email.com', TRUE, 'estoquista', '1234'),
  ('Itallo', '98765432101', 'itallo@email.com', TRUE, 'administrador', '1234');
  
INSERT INTO usuario (nome, cpf, email, status, grupo, senha) VALUES ('douglas', '98765432517', 'douglas22@email.com', TRUE, 'administrador', '1234');
  select * from usuario;
   select * from produto_imagem;
    select * from produto;