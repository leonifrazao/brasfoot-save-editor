package br.com.saveeditor.brasfoot.domain;

import br.com.saveeditor.brasfoot.model.NavegacaoState;

public class SaveContext {
    private NavegacaoState state;
    private String currentFilePath;

    public void load(NavegacaoState state, String path) {
        this.state = state;
        this.currentFilePath = path;
    }

    public boolean isLoaded() {
        return state != null;
    }

    public NavegacaoState getState() {
        return state;
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }
}
