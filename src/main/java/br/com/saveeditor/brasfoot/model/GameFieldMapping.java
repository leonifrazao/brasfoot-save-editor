package br.com.saveeditor.brasfoot.model;

import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import java.util.Arrays;
import java.util.List;

/**
 * Maps user-friendly names to known obfuscated field names in Brasfoot.
 */
public enum GameFieldMapping {

    // Common fields
    NAME("Name/Nome", Arrays.asList(BrasfootConstants.TEAM_NAME, "nome", "name")),
    ID("ID", Arrays.asList(BrasfootConstants.TEAM_ID, "id")),

    // Team Fields
    MONEY("Money/Caixa", Arrays.asList(BrasfootConstants.TEAM_MONEY, "caixa", "dinheiro", "money")),
    REPUTATION("Reputation/Reputação", Arrays.asList(BrasfootConstants.TEAM_REPUTATION, "reputacao", "nc")),
    STADIUM_CAPACITY("Stadium Capacity (Object)", Arrays.asList(BrasfootConstants.TEAM_STADIUM, "estadio", "dH")),
    DIVISION("Division/Divisão", Arrays.asList(BrasfootConstants.TEAM_DIVISION, "divisao")),
    COUNTRY("Country/País", Arrays.asList(BrasfootConstants.TEAM_COUNTRY, "pais")),

    // Player Fields
    STRENGTH("Strength/Força", Arrays.asList(BrasfootConstants.PLAYER_OVERALL, "forca", "strength", "overall")),
    AGE("Age/Idade", Arrays.asList(BrasfootConstants.PLAYER_AGE, "idade", "age")),
    POSITION("Position/Posição", Arrays.asList(BrasfootConstants.PLAYER_POSITION, "posicao")),
    SIDE("Side/Lado", Arrays.asList(BrasfootConstants.PLAYER_SIDE, "lado")),
    ENERGY("Energy/Energia", Arrays.asList(BrasfootConstants.PLAYER_ENERGY, "energia")),
    CONTRACT("Contract/Contrato", Arrays.asList(BrasfootConstants.PLAYER_CONTRACT_END, "contrato", "contract")),
    STAR_LOCAL("Star (Local)", Arrays.asList(BrasfootConstants.PLAYER_STAR_LOCAL, "estrela_local")),
    STAR_GLOBAL("Star (Global)", Arrays.asList(BrasfootConstants.PLAYER_STAR_GLOBAL, "estrela_mundial")),

    // Manager Fields
    CONFIDENCE_BOARD("Confidence (Board)",
            Arrays.asList(BrasfootConstants.MANAGER_CONFIDENCE_BOARD, "confianca_diretoria")),
    CONFIDENCE_FANS("Confidence (Fans)", Arrays.asList(BrasfootConstants.MANAGER_CONFIDENCE_FANS, "confianca_torcida")),
    IS_HUMAN("Is Human?", Arrays.asList(BrasfootConstants.MANAGER_IS_HUMAN, "humano"));

    private final String label;
    private final List<String> possibleFields;

    GameFieldMapping(String label, List<String> possibleFields) {
        this.label = label;
        this.possibleFields = possibleFields;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getPossibleFields() {
        return possibleFields;
    }

    public static GameFieldMapping fromString(String input) {
        for (GameFieldMapping m : values()) {
            if (m.name().equalsIgnoreCase(input) || m.label.toLowerCase().contains(input.toLowerCase())) {
                return m;
            }
        }
        return null; // or throw exception
    }
}
