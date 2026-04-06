package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.adapters.in.web.record.in.ManagerUpdateRequest;
import br.com.saveeditor.brasfoot.adapters.in.web.record.out.ManagerDto;
import br.com.saveeditor.brasfoot.application.ports.in.BatchUpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.GetManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.ManagerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.domain.Manager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/managers")
@Tag(name = "Manager Operations", description = "Endpoints for viewing and editing managers (human and AI) in a loaded session")
public class ManagerController {

    private final GetManagerUseCase getManagerUseCase;
    private final UpdateManagerUseCase updateManagerUseCase;
    private final BatchUpdateManagerUseCase batchUpdateManagerUseCase;

    public ManagerController(GetManagerUseCase getManagerUseCase,
                             UpdateManagerUseCase updateManagerUseCase,
                             BatchUpdateManagerUseCase batchUpdateManagerUseCase) {
        this.getManagerUseCase = getManagerUseCase;
        this.updateManagerUseCase = updateManagerUseCase;
        this.batchUpdateManagerUseCase = batchUpdateManagerUseCase;
    }

    @GetMapping
    @Operation(summary = "Get all managers", description = "Retrieves a list of all managers (human and CPU-controlled) currently loaded in the session.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of managers."),
                   @ApiResponse(responseCode = "404", description = "Session not found.")
               })
    public ResponseEntity<List<ManagerDto>> getAllManagers(@PathVariable String sessionId) {
        List<Manager> managers = getManagerUseCase.getManagers(sessionId);
        List<ManagerDto> dtos = managers.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{managerId}")
    @Operation(summary = "Get a specific manager", description = "Retrieves detailed information of a specific manager by their unique ID.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved manager details."),
                   @ApiResponse(responseCode = "404", description = "Session or manager not found.")
               })
    public ResponseEntity<ManagerDto> getManager(
            @PathVariable String sessionId,
            @PathVariable int managerId) {
        return getManagerUseCase.getManager(sessionId, managerId)
                .map(manager -> ResponseEntity.ok(toDto(manager)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{managerId}")
    @Operation(summary = "Update a manager", description = "Updates specific properties of a manager, such as name, age, reputation, or trophies. Fields left blank will not be updated.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully updated the manager. Returns the updated manager details."),
                   @ApiResponse(responseCode = "400", description = "Invalid input data."),
                   @ApiResponse(responseCode = "404", description = "Session or manager not found.")
               })
    public ResponseEntity<ManagerDto> updateManager(
            @PathVariable String sessionId,
            @PathVariable int managerId,
            @Valid @RequestBody ManagerUpdateRequest request) {
        Manager updateData = toDomain(request);
        Manager updated = updateManagerUseCase.updateManager(sessionId, managerId, updateData);
        return ResponseEntity.ok(toDto(updated));
    }

    @PatchMapping("/batch")
    @Operation(summary = "Batch update managers", description = "Updates multiple managers in a single request. Only supplied fields are changed for each manager.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully updated managers. Returns updated manager details."),
                   @ApiResponse(responseCode = "400", description = "Invalid input data."),
                   @ApiResponse(responseCode = "404", description = "Session or manager not found.")
               })
    public ResponseEntity<List<ManagerDto>> batchUpdateManagers(
            @PathVariable String sessionId,
            @RequestBody List<ManagerBatchUpdateCommand> commands) {
        List<Manager> updatedManagers = batchUpdateManagerUseCase.batchUpdateManagers(sessionId, commands);
        List<ManagerDto> dtos = updatedManagers.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private ManagerDto toDto(Manager manager) {
        ManagerDto dto = new ManagerDto();
        dto.setId(manager.getId());
        dto.setName(manager.getName());
        dto.setIsHuman(manager.getIsHuman());
        dto.setTeamId(manager.getTeamId());
        dto.setConfidenceBoard(manager.getConfidenceBoard());
        dto.setConfidenceFans(manager.getConfidenceFans());
        dto.setAge(manager.getAge());
        dto.setNationality(manager.getNationality());
        dto.setReputation(manager.getReputation());
        dto.setTrophies(manager.getTrophies());
        return dto;
    }

    private Manager toDomain(ManagerUpdateRequest request) {
        Manager manager = new Manager();
        manager.setName(request.getName());
        manager.setAge(request.getAge());
        manager.setNationality(request.getNationality());
        manager.setReputation(request.getReputation());
        manager.setTrophies(request.getTrophies());
        return manager;
    }
}
