package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web;

import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.ManagerMapper;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.PlayerMapper;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.TeamMapper;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.ManagerBatchOperationRequest;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.PlayerBatchOperationRequest;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.SessionBatchOperationRequest;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.TeamBatchOperationRequest;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out.BatchCommandResponse;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out.BatchCommandResult;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionDeletedException;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionExpiredException;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/commands")
@Tag(name = "Batch Commands", description = "Executes mixed batch update commands for managers, teams, and players inside a loaded session")
public class BatchCommandController {

    private final UpdateManagerUseCase updateManagerUseCase;
    private final UpdateTeamUseCase updateTeamUseCase;
    private final UpdatePlayerUseCase updatePlayerUseCase;
    private final ManagerMapper managerMapper;
    private final TeamMapper teamMapper;
    private final PlayerMapper playerMapper;

    public BatchCommandController(UpdateManagerUseCase updateManagerUseCase,
                                  UpdateTeamUseCase updateTeamUseCase,
                                  UpdatePlayerUseCase updatePlayerUseCase,
                                  ManagerMapper managerMapper,
                                  TeamMapper teamMapper,
                                  PlayerMapper playerMapper) {
        this.updateManagerUseCase = updateManagerUseCase;
        this.updateTeamUseCase = updateTeamUseCase;
        this.updatePlayerUseCase = updatePlayerUseCase;
        this.managerMapper = managerMapper;
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
    }

    @PostMapping("/batch")
    @Operation(summary = "Execute mixed batch commands",
            description = "Executes a mixed list of manager, team, and player update commands in a single request. Each item is evaluated independently and returns its own success or error result.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "All commands completed successfully."),
                    @ApiResponse(responseCode = "207", description = "Partial success. At least one command failed."),
                    @ApiResponse(responseCode = "400", description = "Invalid command payload."),
                    @ApiResponse(responseCode = "404", description = "Session not found.")
            })
    public ResponseEntity<BatchCommandResponse> executeBatch(
            @PathVariable UUID sessionId,
            @RequestBody List<SessionBatchOperationRequest> operations) {

        List<BatchCommandResult> results = new ArrayList<>();

        for (int i = 0; i < operations.size(); i++) {
            SessionBatchOperationRequest operation = operations.get(i);
            try {
                results.add(executeOperation(sessionId, i, operation));
            } catch (SessionNotFoundException | SessionDeletedException | SessionExpiredException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                results.add(BatchCommandResult.failure(i, operation.type(), ex.getMessage()));
            }
        }

        boolean anyFailed = results.stream().anyMatch(result -> !result.success());
        HttpStatus status = anyFailed ? HttpStatus.MULTI_STATUS : HttpStatus.OK;
        return ResponseEntity.status(status).body(new BatchCommandResponse(results));
    }

    private BatchCommandResult executeOperation(UUID sessionId, int index, SessionBatchOperationRequest operation) {
        if (operation instanceof ManagerBatchOperationRequest request) {
            return executeManagerUpdate(sessionId, index, request);
        }
        if (operation instanceof TeamBatchOperationRequest request) {
            return executeTeamUpdate(sessionId, index, request);
        }
        if (operation instanceof PlayerBatchOperationRequest request) {
            return executePlayerUpdate(sessionId, index, request);
        }
        throw new IllegalArgumentException("Unsupported operation type: " + operation.type());
    }

    private BatchCommandResult executeManagerUpdate(UUID sessionId, int index, ManagerBatchOperationRequest request) {
        Manager managerUpdate = new Manager();
        managerUpdate.setName(request.name());
        managerUpdate.setConfidenceBoard(request.confidenceBoard());
        managerUpdate.setConfidenceFans(request.confidenceFans());

        Manager updatedManager = updateManagerUseCase.updateManager(sessionId, request.managerId(), managerUpdate);
        return BatchCommandResult.success(index, request.type(), managerMapper.toResponse(updatedManager));
    }

    private BatchCommandResult executeTeamUpdate(UUID sessionId, int index, TeamBatchOperationRequest request) {
        Team updatedTeam = updateTeamUseCase.updateTeam(sessionId, request.teamId(), request.money(), request.reputation());
        return BatchCommandResult.success(index, request.type(), teamMapper.toDto(updatedTeam));
    }

    private BatchCommandResult executePlayerUpdate(UUID sessionId, int index, PlayerBatchOperationRequest request) {
        Player updatedPlayer = updatePlayerUseCase.updatePlayer(
                sessionId,
                request.teamId(),
                request.playerId(),
                request.age(),
                request.overall(),
                request.position(),
                request.energy(),
                request.morale(),
                request.starLocal(),
                request.starGlobal()
        );
        return BatchCommandResult.success(index, request.type(), playerMapper.toDto(updatedPlayer));
    }
}
