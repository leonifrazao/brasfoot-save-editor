package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out;

import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;

public record TeamDto(
    int id,
    String name,
    long money,
    TeamReputation reputation
) {}
