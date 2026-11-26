package br.com.saveeditor.brasfoot.domain.enums;

public enum TeamPlayStyle {
    BALANCED(0, "Equilibrado"),
    FULL_ATTACK(1, "Ataque total"),
    COUNTER_ATTACK(2, "Contra-ataque"),
    POSSESSION(3, "Posse de bola"),
    VERTICAL(4, "Jogo vertical"),
    DIRECT(5, "Jogo direto"),
    HIGH_PRESS(6, "Pressão alta"),
    COUNTER_PRESS(7, "Pressão pós-perda"),
    LOW_BLOCK(8, "Bloco baixo");

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
