package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.adapters.in.web.dto.TeamDto;
import br.com.saveeditor.brasfoot.adapters.in.web.dto.TeamUpdateRequest;
import br.com.saveeditor.brasfoot.application.ports.in.GetTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.domain.Team;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/teams")
@Tag(name = "Team Management", description = "Endpoints for viewing and editing teams in a loaded session")
public class TeamController {

    private final GetTeamUseCase getTeamUseCase;
    private final UpdateTeamUseCase updateTeamUseCase;

    public TeamController(GetTeamUseCase getTeamUseCase, UpdateTeamUseCase updateTeamUseCase) {
        this.getTeamUseCase = getTeamUseCase;
        this.updateTeamUseCase = updateTeamUseCase;
    }

    @GetMapping
    @Operation(summary = "Get all teams", description = "Retrieves a list of all teams in the session")
    public ResponseEntity<List<TeamDto>> getAllTeams(@PathVariable UUID sessionId) {
        List<Team> teams = getTeamUseCase.getAllTeams(sessionId);
        List<TeamDto> dtos = teams.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Get a specific team", description = "Retrieves details of a specific team by its ID")
    public ResponseEntity<TeamDto> getTeam(
            @PathVariable UUID sessionId,
            @PathVariable int teamId) {
        Team team = getTeamUseCase.getTeam(sessionId, teamId);
        return ResponseEntity.ok(mapToDto(team));
    }

    @PatchMapping("/{teamId}")
    @Operation(summary = "Update a team", description = "Updates specific properties of a team, such as money or reputation")
    public ResponseEntity<TeamDto> updateTeam(
            @PathVariable UUID sessionId,
            @PathVariable int teamId,
            @RequestBody TeamUpdateRequest request) {
        
        Team updatedTeam = updateTeamUseCase.updateTeam(
                sessionId,
                teamId,
                request.money(),
                request.reputation()
        );
        
        return ResponseEntity.ok(mapToDto(updatedTeam));
    }

    private TeamDto mapToDto(Team team) {
        return new TeamDto(
                team.id(),
                team.name(),
                team.money(),
                team.reputation()
        );
    }
}
