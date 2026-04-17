package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web;

import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.mapper.ManagerMapper;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in.ManagerUpdateRequest;
import br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out.ManagerResponse;
import br.com.saveeditor.brasfoot.application.ports.in.BatchUpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.GetManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.ManagerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.application.shared.BatchResult;
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
import java.util.UUID;

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
    public ResponseEntity<List<ManagerResponse>> getAllManagers(@PathVariable UUID sessionId) {
        List<Manager> managers = getManagerUseCase.getManagers(sessionId);
        List<ManagerResponse> responses = managerMapper.toResponseList(managers);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{managerId}")
    @Operation(summary = "Get a specific manager", description = "Retrieves detailed information of a specific manager by their unique ID.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully retrieved manager details."),
                   @ApiResponse(responseCode = "404", description = "Session or manager not found.")
               })
    public ResponseEntity<ManagerResponse> getManager(
            @PathVariable UUID sessionId,
            @PathVariable int managerId) {
        return getManagerUseCase.getManager(sessionId, managerId)
                .map(manager -> ResponseEntity.ok(managerMapper.toResponse(manager)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{managerId}")
    @Operation(summary = "Update a manager", description = "Updates specific properties of a manager, such as name, age, reputation, or trophies. Fields left blank will not be updated.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Successfully updated the manager. Returns the updated manager details."),
                   @ApiResponse(responseCode = "400", description = "Invalid input data."),
                   @ApiResponse(responseCode = "404", description = "Session or manager not found.")
               })
    public ResponseEntity<ManagerResponse> updateManager(
            @PathVariable UUID sessionId,
            @PathVariable int managerId,
            @Valid @RequestBody ManagerUpdateRequest request) {
        Manager updateData = managerMapper.toDomain(request);
        Manager updated = updateManagerUseCase.updateManager(sessionId, managerId, updateData);
        return ResponseEntity.ok(managerMapper.toResponse(updated));
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
    public ResponseEntity<BatchResponse<ManagerResponse>> batchUpdateManagers(
            @PathVariable UUID sessionId,
            @RequestBody List<ManagerBatchUpdateCommand> commands) {

        BatchResponse<Manager> response = batchUpdateManagerUseCase.batchUpdateManagers(sessionId, commands);

        List<BatchResult<ManagerResponse>> responseResults = new ArrayList<>();
        for (BatchResult<Manager> result : response.getResults()) {
            if (result.isSuccess()) {
                responseResults.add(BatchResult.success(result.getIndex(), managerMapper.toResponse(result.getData())));
            } else {
                responseResults.add(BatchResult.failure(result.getIndex(), result.getError()));
            }
        }

        BatchResponse<ManagerResponse> responseBody = new BatchResponse<>(responseResults);

        // Determine status: 207 if any failed, 200 if all succeeded
        boolean anyFailed = response.getResults().stream().anyMatch(r -> !r.isSuccess());
        HttpStatus status = anyFailed ? HttpStatus.MULTI_STATUS : HttpStatus.OK;

        return ResponseEntity.status(status).body(responseBody);
    }
}
