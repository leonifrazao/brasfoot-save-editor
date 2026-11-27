package br.com.saveeditor.brasfoot.domain;

/** Additional fields from the player record. A null value in an update leaves the field unchanged. */
public record PlayerExtraAttributes(Integer potential, Integer estimatedPotential, Integer development,
                                    Integer shirtNumber, Integer marketValue, Integer askingPrice,
                                    Boolean listedForSale, Boolean listedForLoan, Long injuredUntil) {
    public static final PlayerExtraAttributes EMPTY =
            new PlayerExtraAttributes(null, null, null, null, null, null, null, null, null);
    public static final int MAX_POTENTIAL = 10;
    public static final int MAX_SHIRT_NUMBER = 99;

    public void validateUpdate() {
        checkRange("Potencial", potential, 1, MAX_POTENTIAL);
        checkRange("Potencial estimado", estimatedPotential, 1, MAX_POTENTIAL);
        checkRange("Desenvolvimento", development, 0, Player.MAX_ATTRIBUTE);
        checkRange("Número da camisa", shirtNumber, 0, MAX_SHIRT_NUMBER);
        checkRange("Valor de mercado", marketValue, 0, Integer.MAX_VALUE);
        checkRange("Preço pedido", askingPrice, 0, Integer.MAX_VALUE);
        if (injuredUntil != null && injuredUntil < 0) {
            throw new IllegalArgumentException("A data de recuperação da lesão é inválida.");
        }
    }

    private static void checkRange(String name, Integer value, int min, int max) {
        if (value != null && (value < min || value > max)) {
            throw new IllegalArgumentException(name + " deve estar entre " + min + " e " + max + ".");
        }
    }
}
