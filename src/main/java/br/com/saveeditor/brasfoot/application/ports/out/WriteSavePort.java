package br.com.saveeditor.brasfoot.application.ports.out;

import br.com.saveeditor.brasfoot.domain.SaveContext;

public interface WriteSavePort {
    byte[] write(SaveContext context);
}