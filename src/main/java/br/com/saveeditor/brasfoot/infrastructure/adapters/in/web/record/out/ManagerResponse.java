package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out;

public record ManagerResponse(
        Integer id,
        String name,
        Boolean isHuman,
        Integer teamId,
        Integer confidenceBoard,
        Integer confidenceFans
) {
}
