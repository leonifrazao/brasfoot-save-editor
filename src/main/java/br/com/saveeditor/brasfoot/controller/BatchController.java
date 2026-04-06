package br.com.saveeditor.brasfoot.controller;

import br.com.saveeditor.brasfoot.dto.BatchRequest;
import br.com.saveeditor.brasfoot.dto.BatchResponse;
import br.com.saveeditor.brasfoot.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/batch")
@Tag(name = "Batch Editing", description = "Endpoints for batch updates of game entities")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    @Operation(summary = "Batch update players and teams", description = "Applies multiple updates across players and teams in a single transaction. Partial failures are possible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "207", description = "Multi-Status - Partial or full success",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BatchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<BatchResponse> processBatch(
            @PathVariable UUID sessionId,
            @RequestBody BatchRequest request) {
        BatchResponse response = batchService.processBatch(sessionId, request);
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
    }
}
