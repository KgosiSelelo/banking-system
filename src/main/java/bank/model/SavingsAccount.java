package bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Pays 0.05% monthly interest. No withdrawals allowed. */
public class SavingsAccount extends Account implements InterestBearing {

    public static final BigDecimal MONTHLY_RATE = new BigDecimal("0.0005");

    public SavingsAccount(Customer owner, String branch) {
        super(owner, branch);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        throw new UnsupportedOperationException(
                "Withdrawals are not allowed from a Savings account");
    }

    @Override
    public BigDecimal calculateInterest() {
        return getBalance().multiply(MONTHLY_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void payInterest() {
        BigDecimal interest = calculateInterest();
        if (interest.signum() > 0) {
            credit(interest, TransactionType.INTEREST, "Monthly interest");
        }
    }

    @Override
    public String getAccountType() { return "Savings"; }
}