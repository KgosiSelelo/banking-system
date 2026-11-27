package bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Pays 5% monthly interest. Needs BWP500.00 to open. Withdrawals allowed. */
public class InvestmentAccount extends Account implements InterestBearing {

    public static final BigDecimal MONTHLY_RATE = new BigDecimal("0.05");
    public static final BigDecimal MIN_OPENING_DEPOSIT = new BigDecimal("500.00");

    public InvestmentAccount(Customer owner, String branch, BigDecimal initialDeposit) {
        super(owner, branch);
        if (initialDeposit == null || initialDeposit.compareTo(MIN_OPENING_DEPOSIT) < 0) {
            owner.removeAccount(this);   // undo registration: the account was not opened
            throw new IllegalArgumentException(
                    "An Investment account needs an opening deposit of at least BWP"
                            + MIN_OPENING_DEPOSIT);
        }
        deposit(initialDeposit, "Opening deposit");
    }

    /** Used by the database layer to rebuild a saved account. */
    public InvestmentAccount(Customer owner, String branch, String accountNumber,
                             LocalDate dateOpened, BigDecimal balance, List<Transaction> history) {
        super(owner, branch, accountNumber, dateOpened, balance, history);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        debit(amount, "Withdrawal");
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
    public String getAccountType() { return "Investment"; }
}