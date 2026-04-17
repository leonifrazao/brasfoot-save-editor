package br.com.saveeditor.brasfoot.adapters.in.web.record.in;

import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;

public record TeamUpdateRequest(
    Long money,
    TeamReputation reputation
) {}
