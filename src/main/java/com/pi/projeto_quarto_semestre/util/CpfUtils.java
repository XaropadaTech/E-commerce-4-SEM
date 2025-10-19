package com.pi.projeto_quarto_semestre.util;

public class CpfUtils {

    public static String somenteDigitos(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    public static boolean isValido(String cpf) {
        cpf = somenteDigitos(cpf);
        if (cpf == null || cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false; // todos iguais

        try {
            int d1 = 0, d2 = 0;
            for (int i = 0; i < 9; i++) {
                int dig = cpf.charAt(i) - '0';
                d1 += dig * (10 - i);
                d2 += dig * (11 - i);
            }
            int r1 = d1 % 11;
            r1 = r1 < 2 ? 0 : 11 - r1;
            d2 += r1 * 2;
            int r2 = d2 % 11;
            r2 = r2 < 2 ? 0 : 11 - r2;
            return (cpf.charAt(9) - '0') == r1 && (cpf.charAt(10) - '0') == r2;
        } catch (Exception e) {
            return false;
        }
    }
}
