package bank.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Turns text typed in the GUI into a validated money amount. */
final class AmountParser {

    private AmountParser() { }

    static BigDecimal parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Enter an amount");
        }
        try {
            BigDecimal value = new BigDecimal(text.trim());
            if (value.scale() > 2) {
                throw new IllegalArgumentException("Use at most 2 decimal places");
            }
            return value.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid amount, for example 250.00");
        }
    }
}