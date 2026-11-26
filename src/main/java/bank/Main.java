package bank;

import bank.controller.*;
import bank.dao.*;
import bank.model.*;

/** Console test of model + controllers + DAOs (no GUI). */
public class Main {

    public static void main(String[] args) {
        CustomerDAO customerDAO = new InMemoryCustomerDAO();
        AccountDAO accountDAO = new InMemoryAccountDAO();
        CustomerController customers = new CustomerController(customerDAO);
        LoginController login = new LoginController(customerDAO);
        AccountController accounts = new AccountController(accountDAO);
        TransactionController tx = new TransactionController(accountDAO);
        InterestController interest = new InterestController(accountDAO);

        customers.register("Neo", "Molefe", "Plot 123, Gaborone", "neo", "secret1");
        Customer neo = login.login("neo", "secret1");
        System.out.println("Logged in: " + neo);

        try { login.login("neo", "wrong"); }
        catch (IllegalArgumentException e) { System.out.println("Bad login rejected: " + e.getMessage()); }

        Account sav = accounts.openSavings(neo, "Main Mall");
        Account inv = accounts.openInvestment(neo, "Main Mall", "1000");
        Account chq = accounts.openCheque(neo, "Main Mall", "Acme Ltd", "Plot 9, Francistown");

        tx.deposit(neo, sav.getAccountNumber(), "2000");
        tx.deposit(neo, chq.getAccountNumber(), "8500.50");
        tx.withdraw(neo, chq.getAccountNumber(), "1200");

        String[] badInputs = {"abc", "-5", "10.555", ""};
        for (String bad : badInputs) {
            try { tx.deposit(neo, sav.getAccountNumber(), bad); }
            catch (IllegalArgumentException e) { System.out.println("Rejected '" + bad + "': " + e.getMessage()); }
        }
        try { tx.withdraw(neo, sav.getAccountNumber(), "50"); }
        catch (RuntimeException e) { System.out.println("Savings withdrawal: " + e.getMessage()); }
        try { tx.withdraw(neo, chq.getAccountNumber(), "999999"); }
        catch (RuntimeException e) { System.out.println("Overdraw: " + e.getMessage()); }
        try { accounts.openInvestment(neo, "Main Mall", "300"); }
        catch (RuntimeException e) { System.out.println("Investment rule: " + e.getMessage()); }

        Customer other = customers.register("Lerato", "Kgosi", "Plot 5, Maun", "lerato", "secret2");
        try { tx.deposit(other, sav.getAccountNumber(), "10"); }
        catch (RuntimeException e) { System.out.println("Ownership: " + e.getMessage()); }

        System.out.println("Accounts paid interest: " + interest.payMonthlyInterest());

        System.out.println();
        for (Account a : accounts.getAccounts(neo)) {
            System.out.println(a);
            tx.history(neo, a.getAccountNumber()).forEach(t -> System.out.println("   " + t));
        }
    }
}