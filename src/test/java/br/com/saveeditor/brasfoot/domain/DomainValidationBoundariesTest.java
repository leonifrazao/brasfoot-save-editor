package br.com.saveeditor.brasfoot.domain;

import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainValidationBoundariesTest {

    @Test
    void teamAcceptsZeroMoney() {
        // Arrange/Act/Assert
        assertDoesNotThrow(() -> new Team(1, "Team", 0L, TeamReputation.MUNICIPAL));
    }

    @Test
    void playerAcceptsBoundaryValues() {
        // Arrange/Act/Assert
        assertDoesNotThrow(() -> new Player(1, "Min", 15, 1, 0, -1, 0));
        assertDoesNotThrow(() -> new Player(2, "Max", 50, 100, 4, 100, 100));
    }

    @Test
    void playerRejectsEnergyBelowMinimum() {
        // Arrange/Act/Assert
        assertThrows(IllegalArgumentException.class, () -> new Player(1, "P", 25, 80, 2, -2, 50));
    }

    @Test
    void playerRejectsMoraleAboveMaximum() {
        // Arrange/Act/Assert
        assertThrows(IllegalArgumentException.class, () -> new Player(1, "P", 25, 80, 2, 50, 101));
    }

    @Test
    void managerAcceptsNullConfidenceValues() {
        // Arrange/Act/Assert
        assertDoesNotThrow(() -> Manager.builder().id(1).name("M").build());
    }

    @Test
    void managerRejectsNegativeBoardConfidence() {
        // Arrange/Act/Assert
        assertThrows(IllegalArgumentException.class, () -> Manager.builder().id(1).confidenceBoard(-1).build());
    }

    @Test
    void managerRejectsFanConfidenceAboveMaximum() {
        // Arrange/Act/Assert
        assertThrows(IllegalArgumentException.class, () -> Manager.builder().id(1).confidenceFans(101).build());
    }
}
