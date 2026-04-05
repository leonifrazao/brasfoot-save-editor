package br.com.saveeditor.brasfoot.domain;

public record Team(
    int id,
    String name,
    long money,
    TeamReputation reputation
) {}

