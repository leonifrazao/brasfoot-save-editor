package br.com.saveeditor.brasfoot.domain;

import br.com.saveeditor.brasfoot.shell.EditorShellContext;
import java.util.UUID;

public record Session(UUID id, EditorShellContext context) {
}