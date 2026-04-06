package br.com.saveeditor.brasfoot.adapters.in.web.record;

import br.com.saveeditor.brasfoot.domain.TeamReputation;

public record TeamBatchUpdateRequest(
    int teamId,
    Long money,
    TeamReputation reputation
) {}
