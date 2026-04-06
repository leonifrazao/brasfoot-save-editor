package br.com.saveeditor.brasfoot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * Post-construct validator called by Builder.
     */
    public static class ManagerBuilder {
        public Manager build() {
            Manager manager = new Manager(id, name, isHuman, teamId, confidenceBoard, confidenceFans, age, nationality, reputation, trophies);
            manager.validate();
            return manager;
        }
    }

    /**
     * Validates confidence values.
     */
    public void validate() {
        validateConfidence("confidenceBoard", confidenceBoard);
        validateConfidence("confidenceFans", confidenceFans);
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
