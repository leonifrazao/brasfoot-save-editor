package br.com.saveeditor.brasfoot.domain;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveContext {
    private NavegacaoState state;
    private String currentFilePath;

    /**
     * Loads a save context with state and file path.
     * Provided for backward compatibility.
     */
    public void load(NavegacaoState state, String path) {
        this.state = state;
        this.currentFilePath = path;
    }

    /**
     * Checks if a save file has been loaded.
     */
    public boolean isLoaded() {
        return state != null;
    }
}
