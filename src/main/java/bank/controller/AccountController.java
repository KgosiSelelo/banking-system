package bank.controller;

import bank.dao.AccountDAO;
import bank.model.*;
import java.util.List;

public class AccountController {

    private final AccountDAO accounts;

    public AccountController(AccountDAO accounts) { this.accounts = accounts; }

    public List<Account> getAccounts(Customer customer) {
        return accounts.findByCustomer(customer.getCustomerId());
    }

    public Account openSavings(Customer customer, String branch) {
        Account a = customer.openSavingsAccount(branch);
        accounts.save(a);
        return a;
    }

    public Account openInvestment(Customer customer, String branch, String depositText) {
        Account a = customer.openInvestmentAccount(branch, AmountParser.parse(depositText));
        accounts.save(a);
        return a;
    }

    public Account openCheque(Customer customer, String branch,
                              String company, String companyAddress) {
        Account a = customer.openChequeAccount(branch, new Employer(company, companyAddress));
        accounts.save(a);
        return a;
    }
}