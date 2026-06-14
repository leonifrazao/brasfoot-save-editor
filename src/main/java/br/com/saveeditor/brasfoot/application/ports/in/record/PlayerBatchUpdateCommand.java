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
    Integer country,
    Integer skillGoalkeeping,
    Integer skillSpeed,
    Integer skillTechnique,
    Integer skillPassing,
    Integer skillTackling,
    Integer skillPlaymaking,
    Integer skillFinishing
) {
    public PlayerBatchUpdateCommand(int playerId, Integer age, Integer overall, Integer position, Integer energy,
                                     Integer morale, Boolean starLocal, Boolean starGlobal) {
        this(playerId, age, overall, position, energy, morale, starLocal, starGlobal, null);
    }

    public PlayerBatchUpdateCommand(int playerId, Integer age, Integer overall, Integer position, Integer energy,
                                    Integer morale, Boolean starLocal, Boolean starGlobal, Integer country) {
        this(playerId, age, overall, position, energy, morale, starLocal, starGlobal, country,
                null, null, null, null, null, null, null);
    }
}
