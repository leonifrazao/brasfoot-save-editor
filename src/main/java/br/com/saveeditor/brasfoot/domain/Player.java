package br.com.saveeditor.brasfoot.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class Player {
    private int id;
    private String name;
    private int age;
    private int overall;
    private int position;
    private int energy;
    private int morale;

    public Player(int id, String name, int age, int overall, int position, int energy, int morale) {
        if (age < 15 || age > 50) {
            throw new IllegalArgumentException("Invalid age: must be between 15 and 50");
        }
        if (overall < 1 || overall > 100) {
            throw new IllegalArgumentException("Invalid overall: must be between 1 and 100");
        }
        if (position < 0 || position > 4) {
            throw new IllegalArgumentException("Invalid position: must be 0 to 4");
        }
        if (energy < -1 || energy > 100) {
            throw new IllegalArgumentException("Invalid energy: must be between -1 and 100");
        }
        if (morale < 0 || morale > 100) {
            throw new IllegalArgumentException("Invalid morale: must be between 0 and 100");
        }
        this.id = id;
        this.name = name;
        this.age = age;
        this.overall = overall;
        this.position = position;
        this.energy = energy;
        this.morale = morale;
    }
}
