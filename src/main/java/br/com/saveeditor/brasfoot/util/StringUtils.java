package br.com.saveeditor.brasfoot.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    /**
     * Remove acentos de uma string e converte para minúsculas para facilitar a
     * busca.
     * Ex: "São Paulo" -> "sao paulo"
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized).replaceAll("").toLowerCase(Locale.ROOT);
    }
}
