package br.com.saveeditor.brasfoot.adapters.in.web.record;

public record PlayerUpdateRequest(
    Integer age,
    Integer overall,
    Integer position,
    Integer energy,
    Integer morale
) {}
