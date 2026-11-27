package bank;

import bank.controller.AccountController;
import bank.controller.CustomerController;
import bank.controller.TransactionController;
import bank.dao.CustomerDAO;
import bank.model.Customer;

/** Fills an empty database with 10 sample customers so the system can be demonstrated. */
public final class SampleData {

    private static final String[][] PEOPLE = {
            {"Neo", "Molefe", "Plot 123, Gaborone"},
            {"Lerato", "Kgosi", "Plot 5, Maun"},
            {"Thabo", "Seretse", "Plot 77, Francistown"},
            {"Kefilwe", "Mothibi", "Plot 9, Palapye"},
            {"Tumelo", "Dikgang", "Plot 31, Serowe"},
            {"Naledi", "Ramotswe", "Plot 48, Lobatse"},
            {"Kagiso", "Modise", "Plot 62, Kasane"},
            {"Masego", "Tau", "Plot 15, Molepolole"},
            {"Onkarabile", "Sebego", "Plot 84, Selebi-Phikwe"},
            {"Boitumelo", "Mogapi", "Plot 20, Mahalapye"}
    };

    public static final String PASSWORD = "pass1234";

    private SampleData() { }

    public static boolean seedIfEmpty(CustomerDAO customerDAO, CustomerController customers,
                                      AccountController accounts, TransactionController tx) {
        if (!customerDAO.findAll().isEmpty()) {
            return false;
        }
        for (int i = 0; i < PEOPLE.length; i++) {
            String[] p = PEOPLE[i];
            Customer c = customers.register(p[0], p[1], p[2], p[0].toLowerCase(), PASSWORD);

            String savings = accounts.openSavings(c, "Main Mall").getAccountNumber();
            tx.deposit(c, savings, String.valueOf(1000 + i * 250));

            if (i % 2 == 0) {
                String invest = accounts.openInvestment(c, "Main Mall",
                        String.valueOf(500 + i * 100)).getAccountNumber();
                tx.deposit(c, invest, "250.00");
            }
            if (i % 3 == 0) {
                String cheque = accounts.openCheque(c, "Main Mall",
                        "Acme Ltd", "Plot 9, Francistown").getAccountNumber();
                tx.deposit(c, cheque, "8500.00");
                tx.withdraw(c, cheque, "1200.00");
            }
        }
        return true;
    }
}