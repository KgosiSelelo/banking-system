package bank.model;

import java.math.BigDecimal;

/** Implemented by any account that earns interest. */
public interface InterestBearing {

    /** Works out this month's interest from the current balance. */
    BigDecimal calculateInterest();

    /** Calculates the interest and credits it to the account. */
    void payInterest();
}