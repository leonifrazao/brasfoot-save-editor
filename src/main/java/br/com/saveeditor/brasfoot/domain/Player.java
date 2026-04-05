package br.com.saveeditor.brasfoot.domain;

public record Player(
    int id,
    String name,
    int age,
    int overall,
    int position,
    int energy,
    int morale
) {}
