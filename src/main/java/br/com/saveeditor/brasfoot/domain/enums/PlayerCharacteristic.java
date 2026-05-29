package br.com.saveeditor.brasfoot.domain.enums;

public enum PlayerCharacteristic {
    POSITIONING(0, "Col", "Colocacao"),
    PENALTY_DEFENSE(1, "DPe", "Defesa Penalty"),
    REFLEX(2, "Ref", "Reflexo"),
    GOALKEEPER_EXIT(3, "SGo", "Saida Gol"),
    PLAYMAKING(4, "Arm", "Armacao"),
    HEADING(5, "Cab", "Cabeceio"),
    CROSSING(6, "Cru", "Cruzamento"),
    TACKLING(7, "Des", "Desarme"),
    DRIBBLING(8, "Dri", "Drible"),
    FINISHING(9, "Fin", "Finalizacao"),
    MARKING(10, "Mar", "Marcacao"),
    PASSING(11, "Pas", "Passe"),
    STAMINA(12, "Res", "Resistencia"),
    SPEED(13, "Vel", "Velocidade");

    private final int code;
    private final String abbreviation;
    private final String label;

    PlayerCharacteristic(int code, String abbreviation, String label) {
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

    public static PlayerCharacteristic fromCode(Integer code) {
        if (code == null) {
            return POSITIONING;
        }
        for (PlayerCharacteristic characteristic : values()) {
            if (characteristic.code == code) {
                return characteristic;
            }
        }
        return POSITIONING;
    }
}
