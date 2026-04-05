package br.com.saveeditor.brasfoot.application.ports.out;

import br.com.saveeditor.brasfoot.shell.EditorShellContext;

public interface WriteSavePort {
    byte[] write(EditorShellContext context);
}