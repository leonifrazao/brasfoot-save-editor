package br.com.saveeditor.brasfoot.application.ports.in.record;

import java.time.LocalDate;

/** Currency values are in whole units, as stored in the save. */
public record PlayerTransferTerms(int fee, LocalDate date, Integer salary, LocalDate contractEnd) {
    public static final PlayerTransferTerms DEFAULT = new PlayerTransferTerms(0, null, null, null);

    public PlayerTransferTerms {
        if (fee < 0) throw new IllegalArgumentException("O valor da transferência deve ser zero ou maior.");
        if (salary != null && salary < 0) throw new IllegalArgumentException("O salário deve ser zero ou maior.");
    }
}
