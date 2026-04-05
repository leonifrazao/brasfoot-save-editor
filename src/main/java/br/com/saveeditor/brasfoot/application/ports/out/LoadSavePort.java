package br.com.saveeditor.brasfoot.application.ports.out;

import br.com.saveeditor.brasfoot.domain.SaveContext;

public interface LoadSavePort {
    SaveContext load(byte[] payload);
}