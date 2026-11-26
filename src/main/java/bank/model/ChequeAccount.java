package bank.model;

import java.math.BigDecimal;

/** For salaried customers. Deposits and withdrawals of any amount, no interest. */
public class ChequeAccount extends Account {

    private final Employer employer;

    public ChequeAccount(Customer owner, String branch, Employer employer) {
        super(owner, branch);
        if (employer == null) {
            owner.removeAccount(this);
            throw new IllegalArgumentException(
                    "A Cheque account can only be opened for an employed customer");
        }
        this.employer = employer;
    }

    @Override
    public void withdraw(BigDecimal amount) {
        debit(amount, "Withdrawal");
    }

    public Employer getEmployer() { return employer; }

    @Override
    public String getAccountType() { return "Cheque"; }
}