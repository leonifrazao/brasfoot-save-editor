package br.com.saveeditor.brasfoot.adapters.in.web;

import br.com.saveeditor.brasfoot.adapters.in.web.mapper.ManagerMapper;
import br.com.saveeditor.brasfoot.adapters.in.web.record.in.ManagerUpdateRequest;
import br.com.saveeditor.brasfoot.adapters.in.web.record.out.ManagerDto;
import br.com.saveeditor.brasfoot.adapters.in.web.record.out.BatchResponse;
import br.com.saveeditor.brasfoot.adapters.in.web.record.out.BatchResult;
import br.com.saveeditor.brasfoot.application.ports.in.BatchUpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.GetManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.ManagerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.domain.Manager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/managers")
@Tag(name = "Manager Operations", description = "Endpoints for viewing and editing managers (human and AI) in a loaded session")
public class ManagerController {

    private final GetManagerUseCase getManagerUseCase;
    private final UpdateManagerUseCase updateManagerUseCase;
    private final BatchUpdateManagerUseCase batchUpdateManagerUseCase;
    private final ManagerMapper managerMapper;

    public ManagerController(GetManagerUseCase getManagerUseCase,
                             UpdateManagerUseCase updateManagerUseCase,
                             BatchUpdateManagerUseCase batchUpdateManagerUseCase,
                             ManagerMapper managerMapper) {
        this.getManagerUseCase = getManagerUseCase;
        this.updateManagerUseCase = updateManagerUseCase;
        this.batchUpdateManagerUseCase = batchUpdateManagerUseCase;
        this.managerMapper = managerMapper;
    }

    @GetMapping
    @Operation(summary = "Get all managers", description = "Retrieves a list of all managers (human and CPU-controlled) currently loaded in the session.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of managers."),
                   @ApiResponse(responseCode = "404", description = "Session not found.")
               })
    public ResponseEntity<List<ManagerDto>> getAllManagers(@PathVariable String sessionId) {
        List<Manager> managers = getManagerUseCase.getManagers(sessionId);
        List<ManagerDto> dtos = managerMapper.toDtoList(managers);
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
                .map(manager -> ResponseEntity.ok(managerMapper.toDto(manager)))
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
        Manager updateData = managerMapper.toDomain(request);
        Manager updated = updateManagerUseCase.updateManager(sessionId, managerId, updateData);
        return ResponseEntity.ok(managerMapper.toDto(updated));
    }

    @PatchMapping("/batch")
    @Operation(summary = "Batch update managers", 
               description = "Updates multiple managers in a single request. Returns 207 Multi-Status if any updates fail, 200 if all succeed. Only supplied fields are changed for each manager.",
               responses = {
                    @ApiResponse(responseCode = "200", description = "All managers successfully updated. Returns successful manager details."),
                    @ApiResponse(responseCode = "207", description = "Partial success. Some managers failed to update. Response includes index-based error mapping."),
                    @ApiResponse(responseCode = "400", description = "Invalid input data."),
                    @ApiResponse(responseCode = "404", description = "Session not found.")
               })
    public ResponseEntity<BatchResponse<ManagerDto>> batchUpdateManagers(
            @PathVariable String sessionId,
            @RequestBody List<ManagerBatchUpdateCommand> commands) {
        
        BatchResponse<Manager> response = batchUpdateManagerUseCase.batchUpdateManagers(sessionId, commands);
        
        // Convert Manager results to ManagerDto
        List<BatchResult<ManagerDto>> dtoResults = new ArrayList<>();
        for (BatchResult<Manager> result : response.getResults()) {
            if (result.isSuccess()) {
                dtoResults.add(BatchResult.success(result.getIndex(), managerMapper.toDto(result.getData())));
            } else {
                dtoResults.add(BatchResult.failure(result.getIndex(), result.getError()));
            }
        }
        
        BatchResponse<ManagerDto> dtoResponse = new BatchResponse<>(dtoResults);
        
        // Determine status: 207 if any failed, 200 if all succeeded
        boolean anyFailed = response.getResults().stream().anyMatch(r -> !r.isSuccess());
        HttpStatus status = anyFailed ? HttpStatus.MULTI_STATUS : HttpStatus.OK;
        
        return ResponseEntity.status(status).body(dtoResponse);
    }
}
