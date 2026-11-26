package br.com.saveeditor.brasfoot.presentation.model;

import java.time.LocalDate;

public record CalendarRoundRow(
        String competitionKey,
        LocalDate date,
        String competition,
        String phase,
        int matchCount
) {
}
