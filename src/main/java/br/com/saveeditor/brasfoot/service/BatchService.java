package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.dto.BatchOperationResult;
import br.com.saveeditor.brasfoot.dto.BatchRequest;
import br.com.saveeditor.brasfoot.dto.BatchResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BatchService {

    private final UpdatePlayerUseCase updatePlayerUseCase;
    private final UpdateTeamUseCase updateTeamUseCase;

    public BatchService(UpdatePlayerUseCase updatePlayerUseCase, UpdateTeamUseCase updateTeamUseCase) {
        this.updatePlayerUseCase = updatePlayerUseCase;
        this.updateTeamUseCase = updateTeamUseCase;
    }

    public BatchResponse processBatch(UUID sessionId, BatchRequest request) {
        BatchResponse response = new BatchResponse();
        List<BatchOperationResult> playerResults = new ArrayList<>();
        List<BatchOperationResult> teamResults = new ArrayList<>();

        if (request.getPlayers() != null) {
            for (BatchRequest.BatchPlayerRequest pReq : request.getPlayers()) {
                try {
                    String[] parts = pReq.getId().split("_");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Player ID must be in format {teamId}_{playerId}");
                    }
                    int teamId = Integer.parseInt(parts[0]);
                    int playerId = Integer.parseInt(parts[1]);

                    updatePlayerUseCase.updatePlayer(
                            sessionId,
                            teamId,
                            playerId,
                            pReq.getData().age(),
                            pReq.getData().overall(),
                            pReq.getData().position(),
                            pReq.getData().energy(),
                            pReq.getData().morale()
                    );
                    playerResults.add(new BatchOperationResult(pReq.getId(), 200, "OK"));
                } catch (Exception e) {
                    playerResults.add(new BatchOperationResult(pReq.getId(), 400, e.getMessage()));
                }
            }
        }

        if (request.getTeams() != null) {
            for (BatchRequest.BatchTeamRequest tReq : request.getTeams()) {
                try {
                    int teamId = Integer.parseInt(tReq.getId());
                    updateTeamUseCase.updateTeam(
                            sessionId,
                            teamId,
                            tReq.getData().money(),
                            tReq.getData().reputation()
                    );
                    teamResults.add(new BatchOperationResult(tReq.getId(), 200, "OK"));
                } catch (Exception e) {
                    teamResults.add(new BatchOperationResult(tReq.getId(), 400, e.getMessage()));
                }
            }
        }

        response.setPlayerResults(playerResults);
        response.setTeamResults(teamResults);
        return response;
    }
}
