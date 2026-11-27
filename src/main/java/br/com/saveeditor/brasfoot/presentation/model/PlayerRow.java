package br.com.saveeditor.brasfoot.presentation.model;

import br.com.saveeditor.brasfoot.domain.PlayerExtraAttributes;

public record PlayerRow(
        int id,
        String name,
        int age,
        int overall,
        int position,
        int energy,
        Integer salary,
        Integer side,
        Long contractEnd,
        Integer country,
        Integer characteristic1,
        Integer characteristic2,
        Integer skillGoalkeeping,
        Integer skillSpeed,
        Integer skillTechnique,
        Integer skillPassing,
        Integer skillTackling,
        Integer skillPlaymaking,
        Integer skillFinishing,
        boolean starLocal,
        boolean starGlobal,
        int draftKey,
        PlayerExtraAttributes extraAttributes
) {
    public PlayerRow(int id, String name, int age, int overall, int position, int energy, Integer salary,
                     Integer side, Long contractEnd, Integer country, Integer characteristic1, Integer characteristic2,
                     Integer skillGoalkeeping, Integer skillSpeed, Integer skillTechnique, Integer skillPassing,
                     Integer skillTackling, Integer skillPlaymaking, Integer skillFinishing,
                     boolean starLocal, boolean starGlobal) {
        this(id, name, age, overall, position, energy, salary, side, contractEnd, country, characteristic1, characteristic2,
                skillGoalkeeping, skillSpeed, skillTechnique, skillPassing, skillTackling, skillPlaymaking,
                skillFinishing, starLocal, starGlobal, id, PlayerExtraAttributes.EMPTY);
    }
}
