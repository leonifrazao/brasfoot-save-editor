package br.com.saveeditor.brasfoot.domain;

public record League(
        String id,
        String name,
        String path,
        int teamCount
) {
}
