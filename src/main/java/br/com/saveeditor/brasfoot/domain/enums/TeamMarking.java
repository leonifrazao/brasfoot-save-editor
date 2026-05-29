package br.com.saveeditor.brasfoot.domain.enums;

public enum TeamMarking {
    LIGHT(0, "Leve"),
    HEAVY(1, "Pesada"),
    VERY_HEAVY(2, "Muito pesada");

    private final int code;
    private final String label;

    TeamMarking(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDisplayName() {
        return code + " - " + label;
    }

    public static TeamMarking fromCode(Integer code) {
        if (code == null) {
            return LIGHT;
        }
        for (TeamMarking marking : values()) {
            if (marking.code == code) {
                return marking;
            }
        }
        return LIGHT;
    }
}
