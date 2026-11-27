package bank.model;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** A bank customer who can hold any number of accounts, of any type. */
public class Customer {

    private final String customerId;
    private String firstName;
    private String surname;
    private String address;
    private final String username;
    private final String salt;
    private final String passwordHash;
    private final List<Account> accounts = new ArrayList<>();

    public Customer(String customerId, String firstName, String surname,
                    String address, String username, String password) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.username = username;
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        this.salt = Base64.getEncoder().encodeToString(saltBytes);
        this.passwordHash = hash(password, saltBytes);
    }

    private Customer(String customerId, String firstName, String surname, String address,
                     String username, String salt, String passwordHash) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    /** Used by the database layer to rebuild a saved customer. */
    public static Customer restore(String customerId, String firstName, String surname,
                                   String address, String username,
                                   String salt, String passwordHash) {
        return new Customer(customerId, firstName, surname, address, username, salt, passwordHash);
    }
    /** Passwords are never stored in plain text. */
    private static String hash(String password, byte[] saltBytes) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 256);
            byte[] bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash password", e);
        }
    }

    public boolean authenticate(String password) {
        if (password == null) return false;
        String attempt = hash(password, Base64.getDecoder().decode(salt));
        return MessageDigest.isEqual(attempt.getBytes(), passwordHash.getBytes());
    }

    public SavingsAccount openSavingsAccount(String branch) {
        return new SavingsAccount(this, branch);
    }

    public InvestmentAccount openInvestmentAccount(String branch, BigDecimal initialDeposit) {
        return new InvestmentAccount(this, branch, initialDeposit);
    }

    public ChequeAccount openChequeAccount(String branch, Employer employer) {
        return new ChequeAccount(this, branch, employer);
    }

    void addAccount(Account account) { accounts.add(account); }
    void removeAccount(Account account) { accounts.remove(account); }

    public List<Account> getAccounts() { return Collections.unmodifiableList(accounts); }

    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public String getUsername() { return username; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Customer c && customerId.equals(c.customerId);
    }

    @Override
    public int hashCode() { return Objects.hash(customerId); }

    @Override
    public String toString() { return firstName + " " + surname + " (" + customerId + ")"; }
}