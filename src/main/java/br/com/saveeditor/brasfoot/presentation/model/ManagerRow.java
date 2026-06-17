package br.com.saveeditor.brasfoot.presentation.model;

import br.com.saveeditor.brasfoot.domain.ManagerTrophy;
import br.com.saveeditor.brasfoot.domain.ManagerTrophyCompetition;

import java.util.List;

public record ManagerRow(
        int id,
        String name,
        Boolean human,
        Integer teamId,
        Integer confidenceBoard,
        Integer confidenceFans,
        List<ManagerTrophy> trophies,
        List<ManagerTrophyCompetition> trophyCompetitions
) {
}
