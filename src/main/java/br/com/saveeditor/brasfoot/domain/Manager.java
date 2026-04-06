package br.com.saveeditor.brasfoot.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
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
     * Setter without validation for id field.
     */
    public void setId(Integer value) {
        this.id = value;
    }

    /**
     * Setter without validation for name field.
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Setter without validation for isHuman field.
     */
    public void setIsHuman(Boolean value) {
        this.isHuman = value;
    }

    /**
     * Setter without validation for teamId field.
     */
    public void setTeamId(Integer value) {
        this.teamId = value;
    }

    /**
     * Setter without validation for age field.
     */
    public void setAge(Integer value) {
        this.age = value;
    }

    /**
     * Setter without validation for nationality field.
     */
    public void setNationality(String value) {
        this.nationality = value;
    }

    /**
     * Setter without validation for reputation field.
     */
    public void setReputation(Integer value) {
        this.reputation = value;
    }

    /**
     * Setter without validation for trophies field.
     */
    public void setTrophies(Integer value) {
        this.trophies = value;
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
