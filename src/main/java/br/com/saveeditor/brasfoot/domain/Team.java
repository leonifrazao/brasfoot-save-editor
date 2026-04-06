package br.com.saveeditor.brasfoot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {
    private int id;
    private String name;
    private long money;
    private TeamReputation reputation;

    /**
     * Validates team attributes.
     */
    public void validate() {
        if (money < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
    }
}
