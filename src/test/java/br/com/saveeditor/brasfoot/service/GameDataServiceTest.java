package br.com.saveeditor.brasfoot.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameDataServiceTest {

    private final GameDataService gameDataService = new GameDataService();

    @Test
    void findTeamIndex_prefersExactNormalizedMatch() {
        // Arrange
        DummyRoot root = new DummyRoot();
        root.aj.add(new DummyTeam(1, "Sao Paulo"));
        root.aj.add(new DummyTeam(2, "Sao Paulo FC"));

        // Act
        int index = gameDataService.findTeamIndex(root, "São Paulo");

        // Assert
        assertEquals(0, index);
    }

    @Test
    void findTeamIndex_fallsBackToStartsWithWhenNoExact() {
        // Arrange
        DummyRoot root = new DummyRoot();
        root.aj.add(new DummyTeam(1, "Corinthians"));
        root.aj.add(new DummyTeam(2, "Palmeiras"));

        // Act
        int index = gameDataService.findTeamIndex(root, "Cor");

        // Assert
        assertEquals(0, index);
    }

    @Test
    void findTeamIndex_fallsBackToContainsWhenNoExactOrStartsWith() {
        // Arrange
        DummyRoot root = new DummyRoot();
        root.aj.add(new DummyTeam(1, "Atletico Mineiro"));
        root.aj.add(new DummyTeam(2, "Cruzeiro"));

        // Act
        int index = gameDataService.findTeamIndex(root, "mine");

        // Assert
        assertEquals(0, index);
    }

    @SuppressWarnings("unused")
    private static class DummyRoot {
        public List<Object> aj = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    private static class DummyTeam {
        public int na;
        public String dm;

        DummyTeam(int id, String name) {
            this.na = id;
            this.dm = name;
        }
    }
}
