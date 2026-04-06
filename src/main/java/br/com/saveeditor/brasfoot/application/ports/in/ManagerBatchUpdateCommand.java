package br.com.saveeditor.brasfoot.application.ports.in;

public record ManagerBatchUpdateCommand(
        int managerId,
        String name,
        Integer confidenceBoard,
        Integer confidenceFans,
        Integer age,
        String nationality,
        Integer reputation,
        Integer trophies
) {
}
