package br.com.saveeditor.brasfoot.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TeamReputation {
    MUNICIPAL(0),
    ESTADUAL(1),
    REGIONAL(2),
    NACIONAL(3),
    CONTINENTAL(4),
    MUNDIAL(5);

    private final int value;

    TeamReputation(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static TeamReputation fromValue(int value) {
        for (TeamReputation r : values()) {
            if (r.value == value) {
                return r;
            }
        }
        // Fallback to highest or a sensible default if the save file has a weird value (like a cheat used 10000)
        if (value > 5) return MUNDIAL;
        if (value < 0) return MUNICIPAL;
        return MUNICIPAL; // Should not reach here
    }
}

