package bank.dao;

import bank.model.Account;
import java.util.List;
import java.util.Optional;

public interface AccountDAO {
    void save(Account account);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomer(String customerId);
    List<Account> findAll();
    void update(Account account);
    void delete(String accountNumber);
}