package br.com.saveeditor.brasfoot.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class Manager {
    private Integer id; // Index in the list
    private String name;
    private Boolean isHuman;
    private Integer teamId;
    private Integer confidenceBoard;
    private Integer confidenceFans;
    private Integer age;
    private String nationality;
    private Integer reputation;
    private Integer trophies;

    public Manager(Integer id, String name, Boolean isHuman, Integer teamId, Integer confidenceBoard, Integer confidenceFans, Integer age, String nationality, Integer reputation, Integer trophies) {
        validateConfidence("confidenceBoard", confidenceBoard);
        validateConfidence("confidenceFans", confidenceFans);
        this.id = id;
        this.name = name;
        this.isHuman = isHuman;
        this.teamId = teamId;
        this.confidenceBoard = confidenceBoard;
        this.confidenceFans = confidenceFans;
        this.age = age;
        this.nationality = nationality;
        this.reputation = reputation;
        this.trophies = trophies;
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
     * Factory method for backward compatibility.
     */
    public static Manager of(Integer id,
                             String name,
                             Boolean isHuman,
                             Integer teamId,
                             Integer confidenceBoard,
                             Integer confidenceFans,
                             Integer age,
                             String nationality,
                             Integer reputation,
                             Integer trophies) {
        return Manager.builder()
                .id(id)
                .name(name)
                .isHuman(isHuman)
                .teamId(teamId)
                .confidenceBoard(confidenceBoard)
                .confidenceFans(confidenceFans)
                .age(age)
                .nationality(nationality)
                .reputation(reputation)
                .trophies(trophies)
                .build();
    }
}
