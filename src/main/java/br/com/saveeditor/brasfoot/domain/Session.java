package br.com.saveeditor.brasfoot.domain;
import java.util.UUID;

public record Session(UUID id, SaveContext context) {
}
