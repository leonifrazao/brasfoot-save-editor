package br.com.saveeditor.brasfoot.presentation.model;

public record ManagerRow(
        int id,
        String name,
        Boolean human,
        Integer teamId,
        Integer confidenceBoard,
        Integer confidenceFans
) {
}
