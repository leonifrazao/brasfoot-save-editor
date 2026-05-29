package br.com.saveeditor.brasfoot.domain.enums;

public enum TeamPlayStyle {
    BALANCED(0, "Equilibrado"),
    FULL_ATTACK(1, "Ataque total"),
    COUNTER_ATTACK(2, "Contra-ataque");

    private final int code;
    private final String label;

    TeamPlayStyle(int code, String label) {
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

    public static TeamPlayStyle fromCode(Integer code) {
        if (code == null) {
            return BALANCED;
        }
        for (TeamPlayStyle style : values()) {
            if (style.code == code) {
                return style;
            }
        }
        return BALANCED;
    }
}
