package br.com.saveeditor.brasfoot.domain.enums;

public enum Continent {
    EUROPE(0),
    SOUTH_AMERICA(1),
    AFRICA(2),
    ASIA(3),
    NORTH_AMERICA(4),
    OCEANIA(5);

    private final int code;

    Continent(int code) { this.code = code; }

    public int getCode() { return code; }

    public static Continent fromCode(int code) {
        for (Continent c : values()) {
            if (c.code == code) return c;
        }
        return null;
    }
}
