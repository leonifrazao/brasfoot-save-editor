package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in;

public record PlayerBatchOperationRequest(
        String type,
        int teamId,
        int playerId,
        Integer age,
        Integer overall,
        Integer position,
        Integer energy,
        Integer morale,
        Boolean starLocal,
        Boolean starGlobal
) implements SessionBatchOperationRequest {
}
