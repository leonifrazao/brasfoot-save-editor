package br.com.saveeditor.brasfoot.domain;

public record ManagerTrophy(
        Integer index,
        Integer year,
        Integer competitionType,
        Integer variant,
        Integer teamId,
        String competitionName
) {
}
