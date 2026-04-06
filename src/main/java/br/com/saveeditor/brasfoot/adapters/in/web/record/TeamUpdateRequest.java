package br.com.saveeditor.brasfoot.adapters.in.web.record;

import br.com.saveeditor.brasfoot.domain.TeamReputation;

public record TeamUpdateRequest(
    Long money,
    TeamReputation reputation
) {}
