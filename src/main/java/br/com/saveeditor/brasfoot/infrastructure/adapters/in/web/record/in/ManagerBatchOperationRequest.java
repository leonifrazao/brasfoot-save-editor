package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in;

public record ManagerBatchOperationRequest(
        String type,
        int managerId,
        String name,
        Integer confidenceBoard,
        Integer confidenceFans
) implements SessionBatchOperationRequest {
}
