package br.com.saveeditor.brasfoot.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
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

    /**
     * Setter with validation for money field.
     */
    public void setMoney(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        this.money = value;
    }

    /**
     * Setter without validation for id field.
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Setter without validation for name field.
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Setter without validation for reputation field.
     */
    public void setReputation(TeamReputation value) {
        this.reputation = value;
    }
}
