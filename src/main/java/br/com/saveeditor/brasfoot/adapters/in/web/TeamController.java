package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.adapters.in.web.dto.TeamDto;
import br.com.saveeditor.brasfoot.adapters.in.web.dto.TeamUpdateRequest;
import br.com.saveeditor.brasfoot.application.ports.in.GetTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.domain.Team;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "Get all teams", description = "Retrieves a comprehensive list of all teams currently loaded in the session.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of teams."),
                   @ApiResponse(responseCode = "404", description = "Session not found.")
               })
    public ResponseEntity<List<TeamDto>> getAllTeams(@PathVariable UUID sessionId) {
        List<Team> teams = getTeamUseCase.getAllTeams(sessionId);
        List<TeamDto> dtos = teams.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Get a specific team", description = "Retrieves detailed information of a specific team by its unique ID.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved team details."),
                   @ApiResponse(responseCode = "404", description = "Session or team not found.")
               })
    public ResponseEntity<TeamDto> getTeam(
            @PathVariable UUID sessionId,
            @PathVariable int teamId) {
        Team team = getTeamUseCase.getTeam(sessionId, teamId);
        return ResponseEntity.ok(mapToDto(team));
    }

    @PatchMapping("/{teamId}")
    @Operation(summary = "Update a team", description = "Updates specific financial and prestige properties of a team, such as money or reputation. Fields left blank will not be updated.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully updated the team. Returns the updated team details."),
                   @ApiResponse(responseCode = "400", description = "Invalid input data."),
                   @ApiResponse(responseCode = "404", description = "Session or team not found.")
               })
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
