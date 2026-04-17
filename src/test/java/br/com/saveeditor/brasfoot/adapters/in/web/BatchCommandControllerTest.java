package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.BatchCommandController;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.ManagerMapper;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.PlayerMapper;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.TeamMapper;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out.ManagerResponse;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out.PlayerDto;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out.TeamDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BatchCommandController.class)
class BatchCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpdateManagerUseCase updateManagerUseCase;

    @MockBean
    private UpdateTeamUseCase updateTeamUseCase;

    @MockBean
    private UpdatePlayerUseCase updatePlayerUseCase;

    @MockBean
    private ManagerMapper managerMapper;

    @MockBean
    private TeamMapper teamMapper;

    @MockBean
    private PlayerMapper playerMapper;

    @Test
    void executeBatch_returnsOkWhenAllOperationsSucceed() throws Exception {
        UUID sessionId = UUID.randomUUID();

        Manager updatedManager = Manager.of(0, "Abel Ferreira", true, 1, 100, 100);
        Team updatedTeam = new Team(12, "Palmeiras", 5_000_000L, TeamReputation.NACIONAL);
        Player updatedPlayer = new Player(3, "Endrick", 18, 90, 4, 95, 100, true, false);

        given(updateManagerUseCase.updateManager(eq(sessionId), eq(0), any(Manager.class))).willReturn(updatedManager);
        given(updateTeamUseCase.updateTeam(sessionId, 12, 5_000_000L, TeamReputation.NACIONAL)).willReturn(updatedTeam);
        given(updatePlayerUseCase.updatePlayer(sessionId, 12, 3, 18, 90, 4, 95, 100, true, false)).willReturn(updatedPlayer);

        given(managerMapper.toResponse(updatedManager)).willReturn(
                new ManagerResponse(0, "Abel Ferreira", true, 1, 100, 100)
        );
        given(teamMapper.toDto(updatedTeam)).willReturn(
                new TeamDto(12, "Palmeiras", 5_000_000L, TeamReputation.NACIONAL)
        );
        given(playerMapper.toDto(updatedPlayer)).willReturn(
                new PlayerDto(3, "Endrick", 18, 90, 4, 95, 100, true, false)
        );

        String payload = """
                [
                  {
                    "type": "manager.update",
                    "managerId": 0,
                    "confidenceBoard": 100,
                    "confidenceFans": 100
                  },
                  {
                    "type": "team.update",
                    "teamId": 12,
                    "money": 5000000,
                    "reputation": 3
                  },
                  {
                    "type": "player.update",
                    "teamId": 12,
                    "playerId": 3,
                    "age": 18,
                    "overall": 90,
                    "position": 4,
                    "energy": 95,
                    "morale": 100,
                    "starLocal": true,
                    "starGlobal": false
                  }
                ]
                """;

        mockMvc.perform(post("/api/v1/sessions/{sessionId}/commands/batch", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].type").value("manager.update"))
                .andExpect(jsonPath("$.results[0].success").value(true))
                .andExpect(jsonPath("$.results[0].data.confidenceBoard").value(100))
                .andExpect(jsonPath("$.results[1].type").value("team.update"))
                .andExpect(jsonPath("$.results[1].data.money").value(5000000))
                .andExpect(jsonPath("$.results[2].type").value("player.update"))
                .andExpect(jsonPath("$.results[2].data.playerId").doesNotExist())
                .andExpect(jsonPath("$.results[2].data.id").value(3));
    }

    @Test
    void executeBatch_returnsMultiStatusWhenOneOperationFails() throws Exception {
        UUID sessionId = UUID.randomUUID();
        Team updatedTeam = new Team(12, "Palmeiras", 5_000_000L, TeamReputation.NACIONAL);

        given(updateManagerUseCase.updateManager(eq(sessionId), eq(99), any(Manager.class)))
                .willThrow(new IllegalArgumentException("Manager not found"));
        given(updateTeamUseCase.updateTeam(sessionId, 12, 5_000_000L, TeamReputation.NACIONAL)).willReturn(updatedTeam);
        given(teamMapper.toDto(updatedTeam)).willReturn(
                new TeamDto(12, "Palmeiras", 5_000_000L, TeamReputation.NACIONAL)
        );

        String payload = """
                [
                  {
                    "type": "manager.update",
                    "managerId": 99,
                    "confidenceBoard": 100
                  },
                  {
                    "type": "team.update",
                    "teamId": 12,
                    "money": 5000000,
                    "reputation": 3
                  }
                ]
                """;

        mockMvc.perform(post("/api/v1/sessions/{sessionId}/commands/batch", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.results[0].success").value(false))
                .andExpect(jsonPath("$.results[0].error").value("Manager not found"))
                .andExpect(jsonPath("$.results[1].success").value(true))
                .andExpect(jsonPath("$.results[1].data.id").value(12));
    }
}
