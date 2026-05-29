package br.com.saveeditor.brasfoot.presentation.model;

public record CountryRow(
        String id,
        String name,
        String group,
        int level,
        int divisionCount
) {
}
