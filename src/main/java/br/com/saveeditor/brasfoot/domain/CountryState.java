package br.com.saveeditor.brasfoot.domain;

public record CountryState(
        String id,
        String name,
        String group,
        int level,
        int divisionCount
) {
}
