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

        assertEquals(1, team.getId());
        assertEquals("Team", team.getName());
        assertEquals(1000L, team.getMoney());
        assertEquals(TeamReputation.NACIONAL, team.getReputation());

        assertEquals(2, player.getId());
        assertEquals("Player", player.getName());
        assertEquals(25, player.getAge());
        assertEquals(88, player.getOverall());

        assertEquals(3, manager.getId());
        assertEquals("Manager", manager.getName());
        assertEquals(50, manager.getConfidenceBoard());
        assertEquals(60, manager.getConfidenceFans());
    }

    // ===== SETTER VALIDATION TESTS =====

    @Test
    void setAgeWithValidValueSucceeds() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertDoesNotThrow(() -> player.setAge(30));
        assertEquals(30, player.getAge());
    }

    @Test
    void setAgeWithInvalidValueThrowsException() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertThrows(IllegalArgumentException.class, () -> player.setAge(14));
        assertThrows(IllegalArgumentException.class, () -> player.setAge(51));
    }

    @Test
    void setOverallWithValidValueSucceeds() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertDoesNotThrow(() -> player.setOverall(95));
        assertEquals(95, player.getOverall());
    }

    @Test
    void setOverallWithInvalidValueThrowsException() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertThrows(IllegalArgumentException.class, () -> player.setOverall(0));
        assertThrows(IllegalArgumentException.class, () -> player.setOverall(101));
    }

    @Test
    void setPositionWithValidValueSucceeds() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertDoesNotThrow(() -> player.setPosition(0));
        assertEquals(0, player.getPosition());
        assertDoesNotThrow(() -> player.setPosition(4));
        assertEquals(4, player.getPosition());
    }

    @Test
    void setPositionWithInvalidValueThrowsException() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertThrows(IllegalArgumentException.class, () -> player.setPosition(-1));
        assertThrows(IllegalArgumentException.class, () -> player.setPosition(5));
    }

    @Test
    void setEnergyWithValidValueSucceeds() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertDoesNotThrow(() -> player.setEnergy(-1));
        assertEquals(-1, player.getEnergy());
        assertDoesNotThrow(() -> player.setEnergy(100));
        assertEquals(100, player.getEnergy());
    }

    @Test
    void setEnergyWithInvalidValueThrowsException() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertThrows(IllegalArgumentException.class, () -> player.setEnergy(-2));
        assertThrows(IllegalArgumentException.class, () -> player.setEnergy(101));
    }

    @Test
    void setMoraleWithValidValueSucceeds() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertDoesNotThrow(() -> player.setMorale(100));
        assertEquals(100, player.getMorale());
    }

    @Test
    void setMoraleWithInvalidValueThrowsException() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertThrows(IllegalArgumentException.class, () -> player.setMorale(-1));
        assertThrows(IllegalArgumentException.class, () -> player.setMorale(101));
    }

    @Test
    void setTeamMoneyWithValidValueSucceeds() {
        Team team = new Team(1, "Team", 1000L, TeamReputation.NACIONAL);
        assertDoesNotThrow(() -> team.setMoney(2000L));
        assertEquals(2000L, team.getMoney());
        assertDoesNotThrow(() -> team.setMoney(0));
        assertEquals(0L, team.getMoney());
    }

    @Test
    void setTeamMoneyWithInvalidValueThrowsException() {
        Team team = new Team(1, "Team", 1000L, TeamReputation.NACIONAL);
        assertThrows(IllegalArgumentException.class, () -> team.setMoney(-1));
    }

    @Test
    void setManagerConfidenceBoardWithValidValueSucceeds() {
        Manager manager = new Manager(1, "Manager", true, 1, 50, 75, 45, "BR", 5, 2);
        assertDoesNotThrow(() -> manager.setConfidenceBoard(80));
        assertEquals(80, manager.getConfidenceBoard());
        assertDoesNotThrow(() -> manager.setConfidenceBoard(null));
        assertEquals(null, manager.getConfidenceBoard());
    }

    @Test
    void setManagerConfidenceBoardWithInvalidValueThrowsException() {
        Manager manager = new Manager(1, "Manager", true, 1, 50, 75, 45, "BR", 5, 2);
        assertThrows(IllegalArgumentException.class, () -> manager.setConfidenceBoard(-1));
        assertThrows(IllegalArgumentException.class, () -> manager.setConfidenceBoard(101));
    }

    @Test
    void setManagerConfidenceFansWithValidValueSucceeds() {
        Manager manager = new Manager(1, "Manager", true, 1, 50, 75, 45, "BR", 5, 2);
        assertDoesNotThrow(() -> manager.setConfidenceFans(60));
        assertEquals(60, manager.getConfidenceFans());
        assertDoesNotThrow(() -> manager.setConfidenceFans(null));
        assertEquals(null, manager.getConfidenceFans());
    }

    @Test
    void setManagerConfidenceFansWithInvalidValueThrowsException() {
        Manager manager = new Manager(1, "Manager", true, 1, 50, 75, 45, "BR", 5, 2);
        assertThrows(IllegalArgumentException.class, () -> manager.setConfidenceFans(-1));
        assertThrows(IllegalArgumentException.class, () -> manager.setConfidenceFans(101));
    }

    @Test
    void settersWithoutValidationAlwaysSucceed() {
        Player player = new Player(1, "Player", 25, 80, 2, 50, 75);
        assertDoesNotThrow(() -> player.setId(999));
        assertEquals(999, player.getId());
        assertDoesNotThrow(() -> player.setName("NewName"));
        assertEquals("NewName", player.getName());

        Team team = new Team(1, "Team", 1000L, TeamReputation.NACIONAL);
        assertDoesNotThrow(() -> team.setId(999));
        assertEquals(999, team.getId());
        assertDoesNotThrow(() -> team.setName("NewTeam"));
        assertEquals("NewTeam", team.getName());

        Manager manager = new Manager(1, "Manager", true, 1, 50, 75, 45, "BR", 5, 2);
        assertDoesNotThrow(() -> manager.setId(999));
        assertEquals(999, manager.getId());
        assertDoesNotThrow(() -> manager.setName("NewManager"));
        assertEquals("NewManager", manager.getName());
    }
}
