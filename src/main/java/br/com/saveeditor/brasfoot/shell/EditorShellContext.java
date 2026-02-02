package br.com.saveeditor.brasfoot.shell;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import org.springframework.stereotype.Component;

@Component
public class EditorShellContext {
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
