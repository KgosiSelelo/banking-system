package bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract parent of every account type.
 * An account cannot exist without a Customer, so the owner is a required
 * constructor argument.
 */
public abstract class Account {

    private static int nextNumber = 1000;

    private final String accountNumber;
    private final String branch;
    private final LocalDate dateOpened;
    private final Customer owner;
    private final List<Transaction> transactions = new ArrayList<>();
    private BigDecimal balance = BigDecimal.ZERO.setScale(2);

    protected Account(Customer owner, String branch) {
        if (owner == null) {
            throw new IllegalArgumentException("An account must belong to a customer");
        }
        this.owner = owner;
        this.branch = branch;
        this.dateOpened = LocalDate.now();
        this.accountNumber = "ACC" + (nextNumber++);
        owner.addAccount(this);
    }
    /** Used by the database layer to rebuild an account that already exists. */
    protected Account(Customer owner, String branch, String accountNumber,
                      LocalDate dateOpened, BigDecimal balance, List<Transaction> history) {
        this.owner = owner;
        this.branch = branch;
        this.accountNumber = accountNumber;
        this.dateOpened = dateOpened;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
        this.transactions.addAll(history);
        owner.addAccount(this);
    }

    /** Keeps new account numbers unique after existing accounts are loaded. */
    public static synchronized void syncNextNumber(int next) {
        if (next > nextNumber) {
            nextNumber = next;
        }
    }
    /** Each account type decides its own withdrawal rules. */
    public abstract void withdraw(BigDecimal amount);

    /** Overload 1: a plain deposit. */
    public void deposit(BigDecimal amount) {
        deposit(amount, "Deposit");
    }

    /** Overload 2: a deposit with a note for the history. */
    public void deposit(BigDecimal amount, String note) {
        requirePositive(amount);
        credit(amount, TransactionType.DEPOSIT, note);
    }

    // Helpers for subclasses

    protected void credit(BigDecimal amount, TransactionType type, String note) {
        BigDecimal amt = amount.setScale(2, RoundingMode.HALF_UP);
        balance = balance.add(amt);
        transactions.add(new Transaction(type, amt, balance, note));
    }

    protected void debit(BigDecimal amount, String note) {
        requirePositive(amount);
        if (amount.compareTo(balance) > 0) {
            throw new IllegalStateException("Insufficient funds: balance is BWP " + balance);
        }
        BigDecimal amt = amount.setScale(2, RoundingMode.HALF_UP);
        balance = balance.subtract(amt);
        transactions.add(new Transaction(TransactionType.WITHDRAWAL, amt, balance, note));
    }

    protected static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    // Getters (no setters: balance only changes through deposit/withdraw)

    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public String getBranch() { return branch; }
    public LocalDate getDateOpened() { return dateOpened; }
    public Customer getOwner() { return owner; }
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public abstract String getAccountType();

    @Override
    public String toString() {
        return getAccountType() + " " + accountNumber + " (BWP " + balance + ")";
    }
}