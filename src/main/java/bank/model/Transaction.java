package bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** An immutable record of one change to an account balance. */
public final class Transaction {

    private final String transactionId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime timestamp;
    private final BigDecimal balanceAfter;
    private final String note;

    public Transaction(TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String note) {
        this.transactionId = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.balanceAfter = balanceAfter;
        this.note = note;
    }
    /** Used by the database layer to rebuild a saved transaction. */
    public Transaction(String transactionId, TransactionType type, BigDecimal amount,
                       LocalDateTime timestamp, BigDecimal balanceAfter, String note) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.balanceAfter = balanceAfter;
        this.note = note;
    }
    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        return String.format("%s | %-10s | BWP %9s | balance BWP %9s | %s",
                timestamp.withNano(0), type, amount, balanceAfter, note);
    }
}