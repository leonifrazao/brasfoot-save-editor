package br.com.saveeditor.brasfoot.domain;

import java.util.UUID;

import lombok.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    private UUID id;
    private SaveContext context;

}
