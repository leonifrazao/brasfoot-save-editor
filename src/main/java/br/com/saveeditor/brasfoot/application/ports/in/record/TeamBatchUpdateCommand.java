package br.com.saveeditor.brasfoot.application.ports.in.record;

import br.com.saveeditor.brasfoot.domain.TeamReputation;

public record TeamBatchUpdateCommand(
    int teamId,
    Long money,
    TeamReputation reputation
) {}
