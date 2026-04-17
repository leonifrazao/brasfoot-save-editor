package br.com.saveeditor.brasfoot.domain;

import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Builder
public class Team {

    @Setter
    private int id;

    @Setter
    private String name;
    private long money;

    @Setter
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


    public void setMoney(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        this.money = value;
    }

}
