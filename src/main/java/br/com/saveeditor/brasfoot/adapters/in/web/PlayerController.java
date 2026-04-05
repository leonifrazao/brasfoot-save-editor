package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.adapters.in.web.dto.PlayerDto;
import br.com.saveeditor.brasfoot.adapters.in.web.dto.PlayerUpdateRequest;
import br.com.saveeditor.brasfoot.application.ports.in.GetPlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.domain.Player;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    public PlayerController(GetPlayerUseCase getPlayerUseCase, UpdatePlayerUseCase updatePlayerUseCase) {
        this.getPlayerUseCase = getPlayerUseCase;
        this.updatePlayerUseCase = updatePlayerUseCase;
    }

    @GetMapping
    @Operation(summary = "Get all players in a team", description = "Retrieves a list of all players for the specified team")
    public ResponseEntity<List<PlayerDto>> getTeamPlayers(
            @PathVariable UUID sessionId,
            @PathVariable int teamId) {
        
        List<Player> players = getPlayerUseCase.getTeamPlayers(sessionId, teamId);
        List<PlayerDto> dtos = players.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{playerId}")
    @Operation(summary = "Get a specific player", description = "Retrieves details of a specific player by ID (index)")
    public ResponseEntity<PlayerDto> getPlayer(
            @PathVariable UUID sessionId,
            @PathVariable int teamId,
            @PathVariable int playerId) {
        
        Player player = getPlayerUseCase.getPlayer(sessionId, teamId, playerId);
        return ResponseEntity.ok(mapToDto(player));
    }

    @PatchMapping("/{playerId}")
    @Operation(summary = "Update a player", description = "Updates specific properties of a player")
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
                request.morale()
        );
        
        return ResponseEntity.ok(mapToDto(updatedPlayer));
    }

    private PlayerDto mapToDto(Player player) {
        return new PlayerDto(
                player.id(),
                player.name(),
                player.age(),
                player.overall(),
                player.position(),
                player.energy(),
                player.morale()
        );
    }
}
