package br.com.saveeditor.brasfoot.presentation.model;

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
        boolean starGlobal
) {
}
