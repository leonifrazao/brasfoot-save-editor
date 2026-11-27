package br.com.saveeditor.brasfoot.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TeamReputation {
    MUNICIPAL(0, "Municipal"),
    ESTADUAL(1, "Estadual"),
    REGIONAL(2, "Regional"),
    NACIONAL(3, "Nacional"),
    CONTINENTAL(4, "Continental"),
    MUNDIAL(5, "Mundial");

    private final int value;
    private final String displayName;

    TeamReputation(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
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

