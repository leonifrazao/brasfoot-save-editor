package br.com.saveeditor.brasfoot.domain.enums;

public enum PlayerSide {
    RIGHT(0, "D", "Direito"),
    LEFT(1, "E", "Esquerdo");

    private final int code;
    private final String abbreviation;
    private final String label;

    PlayerSide(int code, String abbreviation, String label) {
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

    public static PlayerSide fromCode(Integer code) {
        if (code == null) {
            return RIGHT;
        }
        for (PlayerSide side : values()) {
            if (side.code == code) {
                return side;
            }
        }
        return RIGHT;
    }
}
