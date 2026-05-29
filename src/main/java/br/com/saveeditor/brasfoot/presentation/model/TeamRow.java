package br.com.saveeditor.brasfoot.presentation.model;

import java.util.List;

public record TeamRow(
        int id,
        String name,
        String alias,
        long money,
        String reputation,
        Integer country,
        Integer division,
        Integer level,
        String stadiumName,
        Integer stadiumCapacity,
        List<Integer> stadiumSectors,
        Integer tacticStyle,
        Integer tacticMarking,
        Integer tacticFocus
) {
}
