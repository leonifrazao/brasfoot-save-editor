package br.com.saveeditor.brasfoot.application.ports.in.record;

import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;

import java.util.List;

public record TeamBatchUpdateCommand(
    int teamId,
    Long money,
    TeamReputation reputation,
    String stadiumName,
    List<Integer> stadiumSectors,
    Integer level
) {
    public TeamBatchUpdateCommand(int teamId, Long money, TeamReputation reputation) {
        this(teamId, money, reputation, null, null, null);
    }

    public TeamBatchUpdateCommand(int teamId, Long money, TeamReputation reputation, String stadiumName,
                                  List<Integer> stadiumSectors) {
        this(teamId, money, reputation, stadiumName, stadiumSectors, null);
    }
}
