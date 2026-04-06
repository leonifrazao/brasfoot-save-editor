package br.com.saveeditor.brasfoot.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainValidationTest {

    @Test
    void creatingTeamWithNegativeMoneyThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Team(1, "Team", -1L, TeamReputation.NACIONAL)
        );

        assertEquals("Money cannot be negative", exception.getMessage());
    }

    @Test
    void creatingPlayerWithInvalidAgeThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Player(1, "Player", 10, 80, 2, 80, 90)
        );

        assertEquals("Invalid age: must be between 15 and 50", exception.getMessage());
    }

    @Test
    void creatingManagerWithInvalidConfidenceThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Manager.builder()
                        .id(1)
                        .name("Manager")
                        .confidenceBoard(120)
                        .confidenceFans(90)
                        .build()
        );

        assertEquals("Invalid confidenceBoard: must be between 0 and 100", exception.getMessage());
    }

    @Test
    void creatingDomainObjectsWithValidValuesPreservesFields() {
        Team team = assertDoesNotThrow(() -> new Team(1, "Team", 1000L, TeamReputation.NACIONAL));
        Player player = assertDoesNotThrow(() -> new Player(2, "Player", 25, 88, 3, 77, 90));
        Manager manager = assertDoesNotThrow(() -> Manager.builder()
                .id(3)
                .name("Manager")
                .confidenceBoard(50)
                .confidenceFans(60)
                .build());

        assertEquals(1, team.id());
        assertEquals("Team", team.name());
        assertEquals(1000L, team.money());
        assertEquals(TeamReputation.NACIONAL, team.reputation());

        assertEquals(2, player.id());
        assertEquals("Player", player.name());
        assertEquals(25, player.age());
        assertEquals(88, player.overall());

        assertEquals(3, manager.getId());
        assertEquals("Manager", manager.getName());
        assertEquals(50, manager.getConfidenceBoard());
        assertEquals(60, manager.getConfidenceFans());
    }
}
