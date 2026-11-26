package bank.dao;

import bank.model.Customer;
import java.util.*;

/** Temporary storage so the app runs before the database exists. */
public class InMemoryCustomerDAO implements CustomerDAO {

    private final Map<String, Customer> store = new LinkedHashMap<>();

    @Override public void save(Customer c) { store.put(c.getCustomerId(), c); }
    @Override public Optional<Customer> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public Optional<Customer> findByUsername(String username) {
        return store.values().stream()
                .filter(c -> c.getUsername().equalsIgnoreCase(username)).findFirst();
    }
    @Override public List<Customer> findAll() { return new ArrayList<>(store.values()); }
    @Override public void update(Customer c) { store.put(c.getCustomerId(), c); }
    @Override public void delete(String id) { store.remove(id); }
}