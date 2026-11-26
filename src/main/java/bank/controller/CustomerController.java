package bank.controller;

import bank.dao.CustomerDAO;
import bank.model.Customer;

public class CustomerController {

    private final CustomerDAO customers;

    public CustomerController(CustomerDAO customers) { this.customers = customers; }

    public Customer register(String firstName, String surname, String address,
                             String username, String password) {
        if (isBlank(firstName) || isBlank(surname) || isBlank(address) || isBlank(username)) {
            throw new IllegalArgumentException("Please fill in all fields");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (customers.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("That username is already taken");
        }
        String id = String.format("C%03d", customers.findAll().size() + 1);
        Customer c = new Customer(id, firstName.trim(), surname.trim(),
                address.trim(), username.trim(), password);
        customers.save(c);
        return c;
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}