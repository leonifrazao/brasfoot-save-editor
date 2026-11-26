package br.com.saveeditor.brasfoot.domain.enums;

public enum TeamAttackFocus {
    CENTER(0, "Pelo meio"),
    SIDES(1, "Pelos lados"),
    BOTH(2, "Ambos");

    private final int code;
    private final String label;

    TeamAttackFocus(int code, String label) {
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

    public static TeamAttackFocus fromCode(Integer code) {
        if (code == null) {
            return CENTER;
        }
        for (TeamAttackFocus focus : values()) {
            if (focus.code == code) {
                return focus;
            }
        }
        return CENTER;
    }
}
