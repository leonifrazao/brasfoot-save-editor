package br.com.saveeditor.brasfoot.infrastructure.adapters.in.web.record.out;

public record PlayerDto(
    int id,
    String name,
    int age,
    int overall,
    int position,
    int energy,
    int morale,
    boolean starLocal,
    boolean starGlobal
) {}
