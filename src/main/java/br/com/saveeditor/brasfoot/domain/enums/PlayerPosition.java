package br.com.saveeditor.brasfoot.domain.enums;

public enum PlayerPosition {
    GOALKEEPER(0, "G", "Goleiro"),
    FULLBACK(1, "L", "Lateral"),
    DEFENDER(2, "Z", "Zagueiro"),
    MIDFIELDER(3, "M", "Meia"),
    FORWARD(4, "A", "Atacante");

    private final int code;
    private final String abbreviation;
    private final String label;

    PlayerPosition(int code, String abbreviation, String label) {
        this.code = code;
        this.abbreviation = abbreviation;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getLabel() {
        return label;
    }

    public String getDisplayName() {
        return code + " - " + abbreviation + " / " + label;
    }

    public static PlayerPosition fromCode(Integer code) {
        if (code == null) {
            return GOALKEEPER;
        }
        for (PlayerPosition position : values()) {
            if (position.code == code) {
                return position;
            }
        }
        return GOALKEEPER;
    }
}
