package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.adapters.in.web.mapper.TeamMapper;
import br.com.saveeditor.brasfoot.adapters.in.web.record.in.TeamBatchUpdateRequest;
import br.com.saveeditor.brasfoot.adapters.in.web.record.in.TeamUpdateRequest;
import br.com.saveeditor.brasfoot.adapters.in.web.record.out.TeamDto;
import br.com.saveeditor.brasfoot.application.ports.in.GetTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.TeamBatchUpdateCommand;
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
    private final TeamMapper teamMapper;

    public TeamController(GetTeamUseCase getTeamUseCase, UpdateTeamUseCase updateTeamUseCase,
                         TeamMapper teamMapper) {
        this.getTeamUseCase = getTeamUseCase;
        this.updateTeamUseCase = updateTeamUseCase;
        this.teamMapper = teamMapper;
    }

    @GetMapping
    @Operation(summary = "Get all teams", description = "Retrieves a comprehensive list of all teams currently loaded in the session.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of teams."),
                   @ApiResponse(responseCode = "404", description = "Session not found.")
               })
    public ResponseEntity<List<TeamDto>> getAllTeams(@PathVariable UUID sessionId) {
        List<Team> teams = getTeamUseCase.getAllTeams(sessionId);
        List<TeamDto> dtos = teamMapper.toDtoList(teams);
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
        return ResponseEntity.ok(teamMapper.toDto(team));
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
        
        return ResponseEntity.ok(teamMapper.toDto(updatedTeam));
    }

    @PatchMapping("/batch")
    @Operation(summary = "Batch update teams", description = "Updates specific financial and prestige properties for multiple teams in a single transaction. Fields left blank will not be updated.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully updated the teams. Returns the updated teams details."),
                   @ApiResponse(responseCode = "400", description = "Invalid input data."),
                   @ApiResponse(responseCode = "404", description = "Session not found.")
               })
    public ResponseEntity<List<TeamDto>> batchUpdateTeams(
            @PathVariable UUID sessionId,
            @RequestBody List<TeamBatchUpdateRequest> requests) {
        
        List<TeamBatchUpdateCommand> commands = requests.stream()
                .map(req -> new TeamBatchUpdateCommand(req.teamId(), req.money(), req.reputation()))
                .collect(Collectors.toList());

        List<Team> updatedTeams = updateTeamUseCase.batchUpdateTeams(sessionId, commands);
        
        List<TeamDto> dtos = teamMapper.toDtoList(updatedTeams);
                
        return ResponseEntity.ok(dtos);
    }
}
