package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web;

import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.PlayerMapper;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.PlayerBatchUpdateRequest;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.PlayerUpdateRequest;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out.PlayerDto;
import br.com.saveeditor.brasfoot.application.ports.in.GetPlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.application.shared.BatchResult;
import br.com.saveeditor.brasfoot.domain.Player;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/teams/{teamId}/players")
@Tag(name = "Player Management", description = "Endpoints for viewing and editing players in a loaded session")
public class PlayerController {

    private final GetPlayerUseCase getPlayerUseCase;
    private final UpdatePlayerUseCase updatePlayerUseCase;
    private final PlayerMapper playerMapper;

    public PlayerController(GetPlayerUseCase getPlayerUseCase, UpdatePlayerUseCase updatePlayerUseCase,
                           PlayerMapper playerMapper) {
        this.getPlayerUseCase = getPlayerUseCase;
        this.updatePlayerUseCase = updatePlayerUseCase;
        this.playerMapper = playerMapper;
    }

    @GetMapping
    @Operation(summary = "Get all players in a team", description = "Retrieves a list of all players currently playing for the specified team.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of players."),
                   @ApiResponse(responseCode = "404", description = "Session or team not found.")
               })
    public ResponseEntity<List<PlayerDto>> getTeamPlayers(
            @PathVariable UUID sessionId,
            @PathVariable int teamId) {

        List<Player> players = getPlayerUseCase.getTeamPlayers(sessionId, teamId);
        List<PlayerDto> dtos = playerMapper.toDtoList(players);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{playerId}")
    @Operation(summary = "Get a specific player", description = "Retrieves details of a specific player by their ID (index within the team).",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved player details."),
                   @ApiResponse(responseCode = "404", description = "Session, team, or player not found.")
               })
    public ResponseEntity<PlayerDto> getPlayer(
            @PathVariable UUID sessionId,
            @PathVariable int teamId,
            @PathVariable int playerId) {

        Player player = getPlayerUseCase.getPlayer(sessionId, teamId, playerId);
        return ResponseEntity.ok(playerMapper.toDto(player));
    }

    @PatchMapping("/{playerId}")
    @Operation(summary = "Update a player", description = "Updates specific properties of a player, such as age, overall, energy, or morale. Only the fields provided in the request will be updated.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully updated the player. Returns the updated player details."),
                   @ApiResponse(responseCode = "400", description = "Invalid input data."),
                   @ApiResponse(responseCode = "404", description = "Session, team, or player not found.")
               })
    public ResponseEntity<PlayerDto> updatePlayer(
            @PathVariable UUID sessionId,
            @PathVariable int teamId,
            @PathVariable int playerId,
            @RequestBody PlayerUpdateRequest request) {

        Player updatedPlayer = updatePlayerUseCase.updatePlayer(
                sessionId,
                teamId,
                playerId,
                request.age(),
                request.overall(),
                request.position(),
                request.energy(),
                request.morale(),
                request.starLocal(),
                request.starGlobal()
        );

        return ResponseEntity.ok(playerMapper.toDto(updatedPlayer));
    }

    @PatchMapping("/batch")
    @Operation(summary = "Batch update players", description = "Updates specific properties for multiple players of a team in a single transaction. Only the fields provided in the request will be updated.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully updated the players. Returns the updated players details."),
                   @ApiResponse(responseCode = "400", description = "Invalid input data."),
                   @ApiResponse(responseCode = "404", description = "Session or team not found.")
               })
    public ResponseEntity<BatchResponse<PlayerDto>> batchUpdatePlayers(
            @PathVariable UUID sessionId,
            @PathVariable int teamId,
            @RequestBody List<PlayerBatchUpdateRequest> requests) {

        List<PlayerBatchUpdateCommand> commands = requests.stream()
                .map(req -> new PlayerBatchUpdateCommand(req.playerId(), req.age(), req.overall(), req.position(), req.energy(), req.morale(), req.starLocal(), req.starGlobal()))
                .collect(Collectors.toList());

        BatchResponse<Player> response = updatePlayerUseCase.batchUpdatePlayers(sessionId, teamId, commands);

        List<BatchResult<PlayerDto>> responseResults = new java.util.ArrayList<>();
        for (BatchResult<Player> result : response.getResults()) {
            if (result.isSuccess()) {
                responseResults.add(BatchResult.success(result.getIndex(), playerMapper.toDto(result.getData())));
            } else {
                responseResults.add(BatchResult.failure(result.getIndex(), result.getError()));
            }
        }

        boolean anyFailed = response.getResults().stream().anyMatch(r -> !r.isSuccess());
        HttpStatus status = anyFailed ? HttpStatus.MULTI_STATUS : HttpStatus.OK;
        return ResponseEntity.status(status).body(new BatchResponse<>(responseResults));
    }
}
