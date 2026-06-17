package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.ManagerTrophy;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.NavegacaoState;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ManagerManagementServiceTest {

    @Mock
    private SessionStatePort sessionStatePort;

    @Mock
    private GameDataPort gameDataPort;

    @Mock
    private SessionResolver sessionResolver;

    @InjectMocks
    private ManagerManagementService managerManagementService;

    private UUID sessionId;
    private Session mockSession;
    private RootObject rootObject;
    private List<Object> dummyManagersList;
    private List<Object> dummyTeamsList;
    private List<Object> humanTeamsList;

    @BeforeEach
    void setUp() throws Exception {
        sessionId = UUID.randomUUID();
        
        // Mock root object
        rootObject = new RootObject();
        
        dummyManagersList = rootObject.al;
        dummyTeamsList = rootObject.aj;
        humanTeamsList = rootObject.ak;
        
        // Create dummy manager
        Object dummyManager = new DummyManager("Pep Guardiola", true, 85, 90, 50, "Spanish", 1000, 7, 1);
        dummyManagersList.add(dummyManager);

        NavegacaoState mockState = mock(NavegacaoState.class);
        when(mockState.getObjetoRaiz()).thenReturn(rootObject);

        SaveContext mockContext = mock(SaveContext.class);
        when(mockContext.getState()).thenReturn(mockState);

        mockSession = new Session(sessionId, mockContext);

        when(gameDataPort.getManagers(rootObject)).thenReturn(dummyManagersList);
    }

    @Test
    void getManagers_returnsMappedManagers() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);

        List<Manager> managers = managerManagementService.getManagers(sessionId);

        assertNotNull(managers);
        assertEquals(1, managers.size());
        
        Manager mappedManager = managers.get(0);
        assertEquals(0, mappedManager.getId());
        assertEquals("Pep Guardiola", mappedManager.getName());
        assertEquals(true, mappedManager.getIsHuman());
        assertEquals(85, mappedManager.getConfidenceBoard());
        assertEquals(90, mappedManager.getConfidenceFans());
        assertEquals(1, mappedManager.getTeamId());
    }

    @Test
    void getManagers_mapsTrophyCompetitionsFromRootScheduleWithInheritedFields() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);
        DummyCompetitionSchedule schedule = new DummyCompetitionSchedule();
        schedule.u.add(new DummyConcreteCompetition(3, 1, "Alagoano"));
        schedule.u.add(new DummyConcreteCompetition(3, 1, "Mineiro"));
        rootObject.as.add(schedule);

        List<Manager> managers = managerManagementService.getManagers(sessionId);

        assertEquals(2, managers.get(0).getTrophyCompetitions().size());
        assertEquals(3, managers.get(0).getTrophyCompetitions().get(0).competitionType());
        assertEquals(1, managers.get(0).getTrophyCompetitions().get(0).variant());
        assertEquals("Alagoano", managers.get(0).getTrophyCompetitions().get(0).name());
        assertEquals("Mineiro", managers.get(0).getTrophyCompetitions().get(1).name());
    }

    @Test
    void getManager_withValidId_returnsManager() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);

        Optional<Manager> managerOpt = managerManagementService.getManager(sessionId, 0);

        assertTrue(managerOpt.isPresent());
        assertEquals("Pep Guardiola", managerOpt.get().getName());
    }

    @Test
    void getManager_withInvalidId_returnsEmpty() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);

        Optional<Manager> managerOpt = managerManagementService.getManager(sessionId, 99);

        assertFalse(managerOpt.isPresent());
    }

    @Test
    void updateManager_withValidIdAndData_updatesAndSaves() throws Exception {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);

        Manager updateData = new Manager();
        updateData.setName("Jose Mourinho");
        updateData.setConfidenceBoard(99);
        updateData.setConfidenceFans(100);


        Manager updated = managerManagementService.updateManager(sessionId, 0, updateData);

        assertEquals("Jose Mourinho", updated.getName());
        assertEquals(99, updated.getConfidenceBoard());
        assertEquals(100, updated.getConfidenceFans());

        
        verify(sessionStatePort, times(1)).save(mockSession);
        
        // Verify the underlying object was actually changed
        Object dummyManager = dummyManagersList.get(0);
        Field nameField = dummyManager.getClass().getField(BrasfootConstants.MANAGER_NAME);
        assertEquals("Jose Mourinho", nameField.get(dummyManager));
    }
    
    @Test
    void updateManager_withInvalidId_throwsException() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);

        Manager updateData = new Manager();
        updateData.setName("Jose Mourinho");

        assertThrows(IllegalArgumentException.class, () -> {
            managerManagementService.updateManager(sessionId, 99, updateData);
        });
        
        verify(sessionStatePort, never()).save(any());
    }

    @Test
    void updateManager_whenHumanTeamChanges_transfersTeamOwnership() throws Exception {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);
        when(gameDataPort.getTeams(mockSession.getContext().getState().getObjetoRaiz())).thenReturn(dummyTeamsList);
        DummyManager manager = (DummyManager) dummyManagersList.get(0);
        manager.nU = 10;
        DummyTeam oldTeam = new DummyTeam(1, true, 10);
        DummyTeam newTeam = new DummyTeam(2, false, -1);
        dummyTeamsList.add(oldTeam);
        dummyTeamsList.add(newTeam);
        humanTeamsList.add(oldTeam);

        Manager updateData = new Manager();
        updateData.setIsHuman(true);
        updateData.setTeamId(2);

        Manager updated = managerManagementService.updateManager(sessionId, 0, updateData);

        assertEquals(2, updated.getTeamId());
        assertEquals(false, oldTeam.mW);
        assertEquals(-1, oldTeam.na);
        assertNull(oldTeam.mZ);
        assertEquals(true, newTeam.mW);
        assertEquals(10, newTeam.na);
        assertSame(manager, newTeam.mZ);
        assertEquals(2, manager.bW);
        assertSame(newTeam, manager.nV);
        assertFalse(humanTeamsList.contains(oldTeam));
        assertTrue(humanTeamsList.contains(newTeam));
        verify(sessionStatePort).save(mockSession);
    }

    @Test
    void updateManager_whenTargetHasAnotherHumanManager_throwsException() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);
        when(gameDataPort.getTeams(mockSession.getContext().getState().getObjetoRaiz())).thenReturn(dummyTeamsList);
        DummyManager manager = (DummyManager) dummyManagersList.get(0);
        manager.nU = 10;
        DummyManager otherManager = new DummyManager("Carlo Ancelotti", true, 80, 80, 60, "Italian", 900, 11, 2);
        otherManager.nU = 11;
        dummyManagersList.add(otherManager);
        dummyTeamsList.add(new DummyTeam(1, true, 10));
        dummyTeamsList.add(new DummyTeam(2, true, 11));

        Manager updateData = new Manager();
        updateData.setIsHuman(true);
        updateData.setTeamId(2);

        assertThrows(IllegalArgumentException.class, () -> managerManagementService.updateManager(sessionId, 0, updateData));
        verify(sessionStatePort, never()).save(any());
    }

    @Test
    void updateManager_whenHumanTargetIsUnemployed_detachesTeamAndKeepsManagerHuman() throws Exception {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);
        when(gameDataPort.getTeams(mockSession.getContext().getState().getObjetoRaiz())).thenReturn(dummyTeamsList);
        DummyManager manager = (DummyManager) dummyManagersList.get(0);
        manager.nU = 10;
        DummyTeam oldTeam = new DummyTeam(1, true, 10);
        manager.nV = oldTeam;
        dummyTeamsList.add(oldTeam);
        humanTeamsList.add(oldTeam);

        Manager updateData = new Manager();
        updateData.setIsHuman(true);
        updateData.setTeamId(-1);

        Manager updated = managerManagementService.updateManager(sessionId, 0, updateData);

        assertEquals(-1, updated.getTeamId());
        assertTrue(manager.mW);
        assertNull(manager.nV);
        assertEquals(-1, manager.bW);
        assertEquals(false, oldTeam.mW);
        assertEquals(-1, oldTeam.na);
        assertNull(oldTeam.mZ);
        assertFalse(humanTeamsList.contains(oldTeam));
        verify(sessionStatePort).save(mockSession);
    }

    @Test
    void updateManager_withTrophies_replacesTrophyListPreservingExistingObjects() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);
        DummyManager manager = (DummyManager) dummyManagersList.get(0);
        DummyTrophy existing = new DummyTrophy(1, 2, 0, 405, new DummyCompetition("Copa do Mundo"));
        manager.cA.add(existing);

        Manager updateData = new Manager();
        updateData.setTrophies(List.of(
                new ManagerTrophy(0, 3, 2, 0, 936, "Copa do Mundo"),
                new ManagerTrophy(-1, 4, 2, 0, 100, null)
        ));

        Manager updated = managerManagementService.updateManager(sessionId, 0, updateData);

        assertEquals(2, manager.cA.size());
        assertSame(existing, manager.cA.get(0));
        assertEquals(3, existing.ae);
        assertEquals(2, existing.w);
        assertEquals(0, existing.dz);
        assertEquals(936, existing.bW);
        assertSame(existing.Y, ((DummyTrophy) manager.cA.get(1)).Y);
        assertEquals(2, updated.getTrophies().size());
        assertEquals(4, updated.getTrophies().get(1).year());
        verify(sessionStatePort).save(mockSession);
    }

    @Test
    void updateManager_withNewTrophyWithoutCompetitionReference_throwsAndDoesNotSave() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);
        DummyManager manager = (DummyManager) dummyManagersList.get(0);
        DummyTrophy existing = new DummyTrophy(1, 2, 0, 405, new DummyCompetition("Copa do Mundo"));
        manager.cA.add(existing);

        Manager updateData = new Manager();
        updateData.setTrophies(List.of(new ManagerTrophy(-1, 4, 1, 29, 100, null)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> managerManagementService.updateManager(sessionId, 0, updateData));

        assertTrue(exception.getMessage().contains("Competicao do trofeu nao encontrada"));
        assertEquals(1, manager.cA.size());
        verify(sessionStatePort, never()).save(any());
    }

    @Test
    void updateManager_withDuplicateTypeAndVariant_matchesCompetitionByName() {
        when(sessionResolver.loadRequired(sessionId)).thenReturn(mockSession);
        DummyManager manager = (DummyManager) dummyManagersList.get(0);
        DummyConcreteCompetition alagoano = new DummyConcreteCompetition(3, 1, "Alagoano");
        DummyConcreteCompetition mineiro = new DummyConcreteCompetition(3, 1, "Mineiro");
        DummyCompetitionSchedule schedule = new DummyCompetitionSchedule();
        schedule.u.add(alagoano);
        schedule.u.add(mineiro);
        rootObject.as.add(schedule);
        manager.cA.add(new DummyTrophy(1, 3, 1, 15, alagoano));

        Manager updateData = new Manager();
        updateData.setTrophies(List.of(new ManagerTrophy(-1, 3, 3, 1, 15, "Mineiro")));

        managerManagementService.updateManager(sessionId, 0, updateData);

        assertSame(mineiro, ((DummyTrophy) manager.cA.get(0)).Y);
        verify(sessionStatePort).save(mockSession);
    }

    @SuppressWarnings("unused")
    public static class RootObject {
        public final List<Object> al = new ArrayList<>();
        public final List<Object> aj = new ArrayList<>();
        public final List<Object> ak = new ArrayList<>();
        public final List<Object> as = new ArrayList<>();
    }

    public static class DummyCompetitionSchedule {
        public final List<Object> u = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public static class DummyTeam {
        public int mU;
        public Boolean mW;
        public int na;
        public Object mZ;

        public DummyTeam(int id, boolean human, int managerId) {
            this.mU = id;
            this.mW = human;
            this.na = managerId;
        }
    }

    // Helper class to simulate game's obfuscated manager object
    public static class DummyManager {
        public String dm; // Name
        public boolean mW; // isHuman
        public int nU; // ManagerId
        public Object nV; // Current team reference
        public int bW; // Current team id
        public Object nW; // Previous team reference
        public int nX; // Previous team id
        public int of; // ConfidenceBoard
        public int og; // ConfidenceFans
        public int age; 
        public String nationality;
        public int reputation;
        public int trophies;
        public List<Object> cA = new ArrayList<>();

        public DummyManager(String name, boolean isHuman, int confBoard, int confFans, int age, String nationality, int reputation, int trophies, int teamId) {
            this.dm = name;
            this.mW = isHuman;
            this.nU = trophies;
            this.bW = teamId;
            this.of = confBoard;
            this.og = confFans;
            this.age = age;
            this.nationality = nationality;
            this.reputation = reputation;
            this.trophies = trophies;
        }
    }

    public static class DummyTrophy {
        public int ae;
        public int w;
        public int dz;
        public int bW;
        public Object Y;

        public DummyTrophy() {
        }

        public DummyTrophy(int year, int competitionType, int variant, int teamId, Object competition) {
            this.ae = year;
            this.w = competitionType;
            this.dz = variant;
            this.bW = teamId;
            this.Y = competition;
        }
    }

    public static class DummyCompetition {
        public String dm;

        public DummyCompetition(String name) {
            this.dm = name;
        }
    }

    public static class DummyBaseCompetition {
        public int tR;
        public int dz;
        public String dm;

        public DummyBaseCompetition(int type, int variant, String name) {
            this.tR = type;
            this.dz = variant;
            this.dm = name;
        }
    }

    public static class DummyConcreteCompetition extends DummyBaseCompetition {
        public DummyConcreteCompetition(int type, int variant, String name) {
            super(type, variant, name);
        }

        public int ip() {
            return dz;
        }
    }
}
