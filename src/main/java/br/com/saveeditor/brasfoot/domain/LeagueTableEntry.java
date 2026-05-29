package br.com.saveeditor.brasfoot.domain;

public record LeagueTableEntry(
        int position,
        int teamId,
        String teamName,
        int points,
        int played,
        int wins,
        int draws,
        int losses,
        int goalsFor,
        int goalsAgainst,
        int goalDifference
) {
}
