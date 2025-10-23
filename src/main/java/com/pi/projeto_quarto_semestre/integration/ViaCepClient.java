package com.pi.projeto_quarto_semestre.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ViaCepClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> buscar(String cepSomenteDigitos) {
        String url = "https://viacep.com.br/ws/" + cepSomenteDigitos + "/json/";
        return restTemplate.getForObject(url, Map.class);
    }

    public boolean cepValido(String cepSomenteDigitos) {
        try {
            Map<String, Object> resp = buscar(cepSomenteDigitos);
            System.out.println("Resposta do ViaCEP: " + resp);

            if (resp == null || resp.isEmpty()) return false;

            Object erro = resp.get("erro");
            if (erro instanceof Boolean && (Boolean) erro) {
                return false;
            }

            return resp.containsKey("logradouro") && resp.containsKey("localidade");
        } catch (Exception e) {
            System.out.println("Erro ao validar CEP: " + e.getMessage());
            return false;
        }
    }
}