package bank;

import bank.controller.*;
import bank.dao.*;
import bank.model.*;

/** Proves the data is really saved: everything is read back through brand new DAOs. */
public class JdbcCheck {

    public static void main(String[] args) {
        DatabaseManager.initialise();

        CustomerDAO customerDAO = new JdbcCustomerDAO();
        AccountDAO accountDAO = new JdbcAccountDAO(customerDAO);
        CustomerController customers = new CustomerController(customerDAO);
        LoginController login = new LoginController(customerDAO);
        AccountController accounts = new AccountController(accountDAO);
        TransactionController tx = new TransactionController(accountDAO);
        InterestController interest = new InterestController(accountDAO);

        String username = "tester" + (System.currentTimeMillis() % 100000);
        customers.register("Test", "User", "Plot 1, Gaborone", username, "secret1");
        Customer user = login.login(username, "secret1");
        System.out.println("Registered and logged in: " + user);

        Account sav = accounts.openSavings(user, "Main Mall");
        Account inv = accounts.openInvestment(user, "Main Mall", "1000");
        Account chq = accounts.openCheque(user, "Main Mall", "Acme Ltd", "Plot 9, Francistown");
        tx.deposit(user, sav.getAccountNumber(), "2000");
        tx.deposit(user, chq.getAccountNumber(), "8500.50");
        tx.withdraw(user, chq.getAccountNumber(), "1200");
        interest.payMonthlyInterest();

        // Simulate closing and reopening the application: new DAOs, nothing kept in memory
        CustomerDAO customerDAO2 = new JdbcCustomerDAO();
        AccountDAO accountDAO2 = new JdbcAccountDAO(customerDAO2);
        Customer again = new LoginController(customerDAO2).login(username, "secret1");
        TransactionController tx2 = new TransactionController(accountDAO2);

        System.out.println("\nAfter reloading from the database:");
        for (Account a : new AccountController(accountDAO2).getAccounts(again)) {
            System.out.println(a);
            tx2.history(again, a.getAccountNumber()).forEach(t -> System.out.println("   " + t));
        }
    }
}