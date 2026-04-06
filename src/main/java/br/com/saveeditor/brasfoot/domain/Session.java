package br.com.saveeditor.brasfoot.domain;

import java.util.UUID;
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
public class Session {
    private UUID id;
    private SaveContext context;

    /**
     * Simple setter without validation for id field.
     */
    public void setId(UUID value) {
        this.id = value;
    }

    /**
     * Simple setter without validation for context field.
     */
    public void setContext(SaveContext value) {
        this.context = value;
    }
}
