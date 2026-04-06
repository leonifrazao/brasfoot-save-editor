package br.com.saveeditor.brasfoot.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class Team {
    private int id;
    private String name;
    private long money;
    private TeamReputation reputation;

    public Team(int id, String name, long money, TeamReputation reputation) {
        if (money < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        this.id = id;
        this.name = name;
        this.money = money;
        this.reputation = reputation;
    }
}
