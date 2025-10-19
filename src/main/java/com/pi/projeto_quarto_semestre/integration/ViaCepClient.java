package com.pi.projeto_quarto_semestre.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class ViaCepClient {

    private final RestClient rest = RestClient.create(); // Spring 6+. Se estiver em Spring <6, use RestTemplate.

    public Map<String, Object> buscar(String cepSomenteDigitos) {
        // Ex: https://viacep.com.br/ws/09760280/json/
        String url = "https://viacep.com.br/ws/" + cepSomenteDigitos + "/json/";
        return rest.get().uri(url).retrieve().body(Map.class);
    }

    public boolean cepValido(String cepSomenteDigitos) {
        try {
            Map<String, Object> resp = buscar(cepSomenteDigitos);
            if (resp == null) return false;
            Object erro = resp.get("erro");
            return !(erro instanceof Boolean && (Boolean) erro);
        } catch (Exception e) {
            return false;
        }
    }
}
