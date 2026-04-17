package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.application.shared.NavegacaoState;
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
    private List<Object> dummyManagersList;

    @BeforeEach
    void setUp() throws Exception {
        sessionId = UUID.randomUUID();
        
        // Mock root object
        Object rootObject = new Object() {
            public final List<Object> al = new ArrayList<>();
        };
        
        dummyManagersList = (List<Object>) rootObject.getClass().getField(BrasfootConstants.HUMAN_MANAGERS_LIST).get(rootObject);
        
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
        Field ageField = dummyManager.getClass().getField(BrasfootConstants.MANAGER_AGE);
        assertEquals(61, ageField.get(dummyManager));
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

    // Helper class to simulate game's obfuscated manager object
    public static class DummyManager {
        public String dm; // Name
        public boolean mW; // isHuman
        public int of; // ConfidenceBoard
        public int og; // ConfidenceFans
        public int age; 
        public String nationality;
        public int reputation;
        public int trophies;
        public int nU; // TeamId

        public DummyManager(String name, boolean isHuman, int confBoard, int confFans, int age, String nationality, int reputation, int trophies, int teamId) {
            this.dm = name;
            this.mW = isHuman;
            this.of = confBoard;
            this.og = confFans;
            this.age = age;
            this.nationality = nationality;
            this.reputation = reputation;
            this.trophies = trophies;
            this.nU = teamId;
        }
    }
}
