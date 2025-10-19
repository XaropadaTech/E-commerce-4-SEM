package com.pi.projeto_quarto_semestre.util;

public class NomeUtils {
    public static boolean nomeValido(String nomeCompleto) {
        if (nomeCompleto == null) return false;
        String[] parts = nomeCompleto.trim().split("\\s+");
        if (parts.length < 2) return false;
        for (String p : parts) {
            if (p.replaceAll("[^A-Za-zÀ-ÖØ-öø-ÿ]", "").length() < 3) return false;
        }
        return true;
    }
}
