package com.pi.projeto_quarto_semestre.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class SeleniumProdutoTest {

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        driver = new ChromeDriver();
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("CT-S1: Validar Acesso Administrativo e Navegação")
    public void testeAdminAcessaListaDeProdutos() throws InterruptedException {
        // 1. Acessa a home administrativa
        driver.get("http://localhost:8080/paginabko");

        Thread.sleep(1000);

        // 2. Login de Admin
        driver.findElement(By.name("email")).sendKeys("itallo@email.com");
        driver.findElement(By.name("senha")).sendKeys("1234");
        driver.findElement(By.tagName("button")).click();

        // Espera entrar na Dashboard
        Thread.sleep(1500);

        System.out.println("Logado na Dashboard. Procurando botão 'Lista Produtos'...");

        // 3. CLICAR NO BOTÃO (Método Exato)
        // Tenta clicar no link que tem o texto EXATO "Lista Produtos"
        try {
            WebElement botaoLista = driver.findElement(By.linkText("LISTA PRODUTOS"));
            botaoLista.click();
        } catch (Exception e) {
            // Se falhar, vamos imprimir o erro para saber se foi por espaço em branco
            System.out.println("❌ ERRO: Não achou 'Lista Produtos' exato.");
            System.out.println("Dica: Verifique se no HTML não tem espaços, ex: ' Lista Produtos '");
            throw e; // Lança o erro para o teste falhar e vermos o log
        }
        // Espera a página carregar
        Thread.sleep(2000);

        // 4. Validação Final
        String urlFinal = driver.getCurrentUrl();

        // Tenta pegar o título para garantir
        String textoTitulo = "";
        try {
            textoTitulo = driver.findElement(By.tagName("h1")).getText();
        } catch (Exception e) {}

        System.out.println("--- DEBUG FINAL ---");
        System.out.println("Chegou em: " + urlFinal);
        System.out.println("Título: " + textoTitulo);
        System.out.println("-------------------");

        // Verifica se a URL contém "listaProdutos"
        Assertions.assertTrue(urlFinal.contains("listaProdutos"),
                "Erro: O clique não levou para a lista. URL atual: " + urlFinal);

        System.out.println("✅ SUCESSO TOTAL: Fluxo de navegação administrativa validado!");
    }

    @Test
    @DisplayName("CT-S2: Validar Cadastro de Produto com Sucesso")
    public void testeCadastrarNovoProduto() throws InterruptedException {
        // 1. Login (O mesmo de sempre)
        driver.get("http://localhost:8080/paginabko");
        Thread.sleep(1000);

        driver.findElement(By.name("email")).sendKeys("itallo@email.com");
        driver.findElement(By.name("senha")).sendKeys("1234");
        driver.findElement(By.tagName("button")).click();
        Thread.sleep(1500);

        // 2. Ir para Lista de Produtos
        // (Usando o método que funcionou para você: Link Text em Maiúsculo ou Partial)
        driver.findElement(By.partialLinkText("LISTA PRODUTOS")).click();
        Thread.sleep(1500);

        // 3. Clicar no botão "+"

        driver.findElement(By.partialLinkText("➕")).click();

        Thread.sleep(1500);

        // Preencher o Formulário
        driver.findElement(By.name("nome")).sendKeys("Produto Teste Selenium");
        driver.findElement(By.name("preco")).sendKeys("99.90");
        driver.findElement(By.name("avaliacao")).sendKeys("5.0"); // Se houver esse campo
        driver.findElement(By.name("qtdEstoque")).sendKeys("100");
        driver.findElement(By.name("descricao")).sendKeys("Descrição gerada automaticamente pelo robô.");

        // Usa CSS Selector para achar o botão de submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(2000);

        // 7. Validação: Voltou para a lista?
        String urlFinal = driver.getCurrentUrl();
        Assertions.assertTrue(urlFinal.contains("listaProdutos"), "Deveria voltar para a lista após salvar!");

        Thread.sleep(2000);

        // Validação Extra: O produto aparece na tabela?
        String corpoPagina = driver.findElement(By.tagName("body")).getText();
        Assertions.assertTrue(corpoPagina.contains("Produto Teste Selenium"), "O novo produto deveria aparecer na lista!");

        Thread.sleep(2000);

        System.out.println("✅ SUCESSO: Produto cadastrado automaticamente!");
    }

    @Test
    @DisplayName("CT-S3: Validar Tentativa de Cadastro (Variante)")
    public void testeTentativaCadastrarNovoProduto() throws InterruptedException {
        // 1. Login (O mesmo de sempre)
        driver.get("http://localhost:8080/paginabko");
        Thread.sleep(1000);

        driver.findElement(By.name("email")).sendKeys("itallo@email.com");
        driver.findElement(By.name("senha")).sendKeys("1234");
        driver.findElement(By.tagName("button")).click();
        Thread.sleep(1500);

        // 2. Ir para Lista de Produtos
        // (Usando o método que funcionou para você: Link Text em Maiúsculo ou Partial)
        driver.findElement(By.partialLinkText("LISTA PRODUTOS")).click();
        Thread.sleep(1500);

        // 3. Clicar no botão "+"

        driver.findElement(By.partialLinkText("➕")).click();

        Thread.sleep(1500);

        // Preencher o Formulário
        driver.findElement(By.name("nome")).sendKeys("Produto Teste Selenium");
        driver.findElement(By.name("preco")).sendKeys("99.90");
        driver.findElement(By.name("avaliacao")).sendKeys("6.0"); // Se houver esse campo
        driver.findElement(By.name("qtdEstoque")).sendKeys("100");
        driver.findElement(By.name("descricao")).sendKeys("Descrição gerada automaticamente pelo robô.");

        // Usa CSS Selector para achar o botão de submit
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(2000);

        // 7. Validação: Voltou para a lista?
        String urlFinal = driver.getCurrentUrl();
        Assertions.assertTrue(urlFinal.contains("listaProdutos"), "Deveria voltar para a lista após salvar!");

        Thread.sleep(2000);

        // Validação Extra: O produto aparece na tabela?
        String corpoPagina = driver.findElement(By.tagName("body")).getText();
        Assertions.assertTrue(corpoPagina.contains("Produto Teste Selenium"), "O novo produto deveria aparecer na lista!");

        Thread.sleep(2000);

        System.out.println("✅ SUCESSO: Produto cadastrado automaticamente!");
    }
}
