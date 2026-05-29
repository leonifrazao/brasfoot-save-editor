package br.com.saveeditor.brasfoot.domain;

import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
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
}
