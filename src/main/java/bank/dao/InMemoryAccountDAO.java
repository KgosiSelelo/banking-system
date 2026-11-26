package bank.dao;

import bank.model.Account;
import java.util.*;

public class InMemoryAccountDAO implements AccountDAO {

    private final Map<String, Account> store = new LinkedHashMap<>();

    @Override public void save(Account a) { store.put(a.getAccountNumber(), a); }
    @Override public Optional<Account> findByAccountNumber(String no) { return Optional.ofNullable(store.get(no)); }
    @Override public List<Account> findByCustomer(String customerId) {
        List<Account> result = new ArrayList<>();
        for (Account a : store.values()) {
            if (a.getOwner().getCustomerId().equals(customerId)) result.add(a);
        }
        return result;
    }
    @Override public List<Account> findAll() { return new ArrayList<>(store.values()); }
    @Override public void update(Account a) { store.put(a.getAccountNumber(), a); }
    @Override public void delete(String no) { store.remove(no); }
}