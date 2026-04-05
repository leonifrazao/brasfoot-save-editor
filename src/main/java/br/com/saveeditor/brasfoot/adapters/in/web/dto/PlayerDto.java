package br.com.saveeditor.brasfoot.adapters.in.web.dto;

public record PlayerDto(
    int id,
    String name,
    int age,
    int overall,
    int position,
    int energy,
    int morale
) {}
