package br.com.saveeditor.brasfoot.application.ports.in.record;

public record PlayerBatchUpdateCommand(
    int playerId,
    Integer age,
    Integer overall,
    Integer position,
    Integer energy,
    Integer morale,
    Boolean starLocal,
    Boolean starGlobal
) {}
