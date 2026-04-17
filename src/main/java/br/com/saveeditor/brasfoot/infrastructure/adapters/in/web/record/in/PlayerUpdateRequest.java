package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.in;

public record PlayerUpdateRequest(
    Integer age,
    Integer overall,
    Integer position,
    Integer energy,
    Integer morale,
    Boolean starLocal,
    Boolean starGlobal
) {}
