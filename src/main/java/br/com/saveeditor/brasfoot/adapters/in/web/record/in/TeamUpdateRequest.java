package br.com.saveeditor.brasfoot.adapters.in.web.record.in;

import br.com.saveeditor.brasfoot.domain.TeamReputation;

public record TeamUpdateRequest(
    Long money,
    TeamReputation reputation
) {}
