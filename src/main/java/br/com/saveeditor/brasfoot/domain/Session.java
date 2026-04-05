package br.com.saveeditor.brasfoot.domain;

import br.com.saveeditor.brasfoot.domain.SaveContext;
import java.util.UUID;

public record Session(UUID id, SaveContext context) {
}