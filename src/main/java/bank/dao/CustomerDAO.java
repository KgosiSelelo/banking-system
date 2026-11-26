package bank.dao;

import bank.model.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    void save(Customer customer);
    Optional<Customer> findById(String customerId);
    Optional<Customer> findByUsername(String username);
    List<Customer> findAll();
    void update(Customer customer);
    void delete(String customerId);
}