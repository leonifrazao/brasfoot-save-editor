package br.com.saveeditor.brasfoot.domain;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Builder
public class Manager {

    @Setter
    private Integer id; // Index in the list

    @Setter
    private String name;
    private Boolean isHuman;

    @Setter
    private Integer teamId;
    private Integer confidenceBoard;
    private Integer confidenceFans;

    public Manager(Integer id, String name, Boolean isHuman, Integer teamId, Integer confidenceBoard, Integer confidenceFans) {
        validateConfidence("confidenceBoard", confidenceBoard);
        validateConfidence("confidenceFans", confidenceFans);
        this.id = id;
        this.name = name;
        this.isHuman = isHuman;
        this.teamId = teamId;
        this.confidenceBoard = confidenceBoard;
        this.confidenceFans = confidenceFans;
    }

    private void validateConfidence(String fieldName, Integer value) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": must be between 0 and 100");
        }
    }

    /**
     * Setter with validation for confidenceBoard field.
     */
    public void setConfidenceBoard(Integer value) {
        validateConfidence("confidenceBoard", value);
        this.confidenceBoard = value;
    }

    /**
     * Setter with validation for confidenceFans field.
     */
    public void setConfidenceFans(Integer value) {
        validateConfidence("confidenceFans", value);
        this.confidenceFans = value;
    }


    /**
     * Factory method for backward compatibility.
     */
    public static Manager of(Integer id,
                             String name,
                             Boolean isHuman,
                             Integer teamId,
                             Integer confidenceBoard,
                             Integer confidenceFans) {
        return Manager.builder()
                .id(id)
                .name(name)
                .isHuman(isHuman)
                .teamId(teamId)
                .confidenceBoard(confidenceBoard)
                .confidenceFans(confidenceFans).build();
    }
}
