package bank.controller;

import bank.dao.AccountDAO;
import bank.model.Account;
import bank.model.Customer;
import bank.model.Transaction;
import java.util.List;

public class TransactionController {

    private final AccountDAO accounts;

    public TransactionController(AccountDAO accounts) { this.accounts = accounts; }

    public void deposit(Customer customer, String accountNumber, String amountText) {
        Account a = findOwned(customer, accountNumber);
        a.deposit(AmountParser.parse(amountText));
        accounts.update(a);
    }

    public void withdraw(Customer customer, String accountNumber, String amountText) {
        Account a = findOwned(customer, accountNumber);
        a.withdraw(AmountParser.parse(amountText));
        accounts.update(a);
    }

    public List<Transaction> history(Customer customer, String accountNumber) {
        return findOwned(customer, accountNumber).getTransactions();
    }

    /** A customer may only touch their own accounts. */
    private Account findOwned(Customer customer, String accountNumber) {
        Account a = accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!a.getOwner().equals(customer)) {
            throw new IllegalArgumentException("That account does not belong to you");
        }
        return a;
    }
}