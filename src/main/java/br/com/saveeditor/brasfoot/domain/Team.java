package br.com.saveeditor.brasfoot.domain;

import lombok.Builder;

public record Team(
    int id,
    String name,
    long money,
    TeamReputation reputation
) {

    @Builder
    public Team {
        if (money < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
    }
}
