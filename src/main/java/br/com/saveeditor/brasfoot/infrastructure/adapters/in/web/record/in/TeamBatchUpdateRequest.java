package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in;

import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;

public record TeamBatchUpdateRequest(
    int teamId,
    Long money,
    TeamReputation reputation
) {}
