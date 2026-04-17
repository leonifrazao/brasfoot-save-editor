package br.com.saveeditor.brasfoot.domain;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Builder
public class Player {

    @Setter
    private int id;

    @Setter
    private String name;
    private int age;
    private int overall;
    private int position;
    private int energy;
    private int morale;
    private boolean starLocal;
    private boolean starGlobal;

    public Player(int id, String name, int age, int overall, int position, int energy, int morale) {
        this(id, name, age, overall, position, energy, morale, false, false);
    }

    public Player(int id, String name, int age, int overall, int position, int energy, int morale, boolean starLocal, boolean starGlobal) {
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
        this.starLocal = starLocal;
        this.starGlobal = starGlobal;
    }


    public void setAge(int value) {
        if (value < 15 || value > 50) {
            throw new IllegalArgumentException("Invalid age: must be between 15 and 50");
        }
        this.age = value;
    }


    public void setOverall(int value) {
        if (value < 1 || value > 100) {
            throw new IllegalArgumentException("Invalid overall: must be between 1 and 100");
        }
        this.overall = value;
    }


    public void setPosition(int value) {
        if (value < 0 || value > 4) {
            throw new IllegalArgumentException("Invalid position: must be 0 to 4");
        }
        this.position = value;
    }


    public void setEnergy(int value) {
        if (value < -1 || value > 100) {
            throw new IllegalArgumentException("Invalid energy: must be between -1 and 100");
        }
        this.energy = value;
    }


    public void setMorale(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Invalid morale: must be between 0 and 100");
        }
        this.morale = value;
    }

    public void setStarLocal(boolean starLocal) {
        this.starLocal = starLocal;
    }

    public void setStarGlobal(boolean starGlobal) {
        this.starGlobal = starGlobal;
    }

}
