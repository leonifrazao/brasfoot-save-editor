package br.com.saveeditor.brasfoot.adapters.in.web.record.in;

import br.com.saveeditor.brasfoot.domain.TeamReputation;

public record TeamBatchUpdateRequest(
    int teamId,
    Long money,
    TeamReputation reputation
) {}
