package bank.controller;

import bank.dao.CustomerDAO;
import bank.model.Customer;

public class LoginController {

    private final CustomerDAO customers;

    public LoginController(CustomerDAO customers) { this.customers = customers; }

    public Customer login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Enter your username and password");
        }
        Customer c = customers.findByUsername(username.trim()).orElse(null);
        if (c == null || !c.authenticate(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return c;
    }
}