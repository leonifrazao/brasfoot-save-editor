package br.com.saveeditor.brasfoot.application.ports.in.record;

public record PlayerBatchUpdateCommand(
    int playerId,
    Integer age,
    Integer overall,
    Integer position,
    Integer energy,
    Integer morale,
    Boolean starLocal,
    Boolean starGlobal,
    Integer country
) {
    public PlayerBatchUpdateCommand(int playerId, Integer age, Integer overall, Integer position, Integer energy,
                                    Integer morale, Boolean starLocal, Boolean starGlobal) {
        this(playerId, age, overall, position, energy, morale, starLocal, starGlobal, null);
    }
}
