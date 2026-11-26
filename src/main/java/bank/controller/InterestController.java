package bank.controller;

import bank.dao.AccountDAO;
import bank.model.Account;
import bank.model.InterestBearing;

public class InterestController {

    private final AccountDAO accounts;

    public InterestController(AccountDAO accounts) { this.accounts = accounts; }

    /** Pays interest to every InterestBearing account; returns how many were paid. */
    public int payMonthlyInterest() {
        int paid = 0;
        for (Account a : accounts.findAll()) {
            if (a instanceof InterestBearing ib) {
                ib.payInterest();
                accounts.update(a);
                paid++;
            }
        }
        return paid;
    }
}