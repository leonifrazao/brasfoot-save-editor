package br.com.saveeditor.brasfoot.domain;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveContext {
    private NavegacaoState state;
    private String currentFilePath;

    /**
     * Simple setter without validation for state field.
     */
    public void setState(NavegacaoState value) {
        this.state = value;
    }

    /**
     * Simple setter without validation for currentFilePath field.
     */
    public void setCurrentFilePath(String value) {
        this.currentFilePath = value;
    }

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
