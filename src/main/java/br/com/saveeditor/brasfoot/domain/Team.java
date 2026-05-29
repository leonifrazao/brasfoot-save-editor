package br.com.saveeditor.brasfoot.domain;

import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.domain.enums.TeamAttackFocus;
import br.com.saveeditor.brasfoot.domain.enums.TeamMarking;
import br.com.saveeditor.brasfoot.domain.enums.TeamPlayStyle;
import lombok.*;

import java.util.List;

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
    @Setter
    private String alias;
    @Setter
    private Integer country;
    @Setter
    private Integer division;
    @Setter
    private Integer level;
    private long money;

    @Setter
    private TeamReputation reputation;
    private String stadiumName;
    private Integer stadiumCapacity;
    private List<Integer> stadiumSectors;
    private Integer tacticStyle;
    private Integer tacticMarking;
    private Integer tacticFocus;

    public Team(int id, String name, long money, TeamReputation reputation) {
        this(id, name, money, reputation, null, null, null);
    }

    public Team(int id, String name, long money, TeamReputation reputation, String stadiumName,
                Integer stadiumCapacity, List<Integer> stadiumSectors) {
        if (money < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        validateStadium(stadiumCapacity, stadiumSectors);
        this.id = id;
        this.name = name;
        this.money = money;
        this.reputation = reputation;
        this.stadiumName = stadiumName;
        this.stadiumCapacity = stadiumCapacity;
        this.stadiumSectors = stadiumSectors == null ? null : List.copyOf(stadiumSectors);
    }

    public Team(int id, String name, String alias, Integer country, Integer division, Integer level, long money,
                TeamReputation reputation, String stadiumName, Integer stadiumCapacity, List<Integer> stadiumSectors,
                Integer tacticStyle, Integer tacticMarking, Integer tacticFocus) {
        this(id, name, money, reputation, stadiumName, stadiumCapacity, stadiumSectors);
        this.alias = alias;
        this.country = country;
        this.division = division;
        this.level = level;
        setTacticStyle(tacticStyle);
        setTacticMarking(tacticMarking);
        setTacticFocus(tacticFocus);
    }

    public void setTacticStyle(Integer tacticStyle) {
        if (tacticStyle != null && TeamPlayStyle.fromCode(tacticStyle).getCode() != tacticStyle) {
            throw new IllegalArgumentException("tacticStyle must be between 0 and 2");
        }
        this.tacticStyle = tacticStyle;
    }

    public void setTacticMarking(Integer tacticMarking) {
        if (tacticMarking != null && TeamMarking.fromCode(tacticMarking).getCode() != tacticMarking) {
            throw new IllegalArgumentException("tacticMarking must be between 0 and 2");
        }
        this.tacticMarking = tacticMarking;
    }

    public void setTacticFocus(Integer tacticFocus) {
        if (tacticFocus != null && TeamAttackFocus.fromCode(tacticFocus).getCode() != tacticFocus) {
            throw new IllegalArgumentException("tacticFocus must be between 0 and 1");
        }
        this.tacticFocus = tacticFocus;
    }


    public void setMoney(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        this.money = value;
    }

    private static void validateStadium(Integer stadiumCapacity, List<Integer> stadiumSectors) {
        if (stadiumCapacity != null && stadiumCapacity < 0) {
            throw new IllegalArgumentException("Stadium capacity cannot be negative");
        }
        if (stadiumSectors == null) {
            return;
        }
        if (stadiumSectors.size() != 4) {
            throw new IllegalArgumentException("Stadium sectors must contain exactly 4 values");
        }
        if (stadiumSectors.stream().anyMatch(value -> value == null || value < 0)) {
            throw new IllegalArgumentException("Stadium sectors cannot contain negative values");
        }
    }

}
