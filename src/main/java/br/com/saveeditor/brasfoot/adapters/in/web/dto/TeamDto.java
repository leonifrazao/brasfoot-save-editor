package br.com.saveeditor.brasfoot.adapters.in.web.dto;

import br.com.saveeditor.brasfoot.domain.TeamReputation;

public record TeamDto(
    int id,
    String name,
    long money,
    TeamReputation reputation
) {}
