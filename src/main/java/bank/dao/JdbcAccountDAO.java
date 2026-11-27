package bank.dao;

import bank.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Stores accounts, employers and transactions in the database using JDBC. */
public class JdbcAccountDAO implements AccountDAO {

    private static final String SELECT_ACCOUNT =
            "SELECT a.account_number, a.account_type, a.branch, a.date_opened, a.balance, "
                    + "a.customer_id, e.company_name, e.company_address "
                    + "FROM account a LEFT JOIN employer e ON a.employer_id = e.employer_id ";

    private final CustomerDAO customers;

    public JdbcAccountDAO(CustomerDAO customers) {
        this.customers = customers;
        syncAccountNumbers();
    }

    /** Makes sure newly created accounts never reuse a number already in the database. */
    private void syncAccountNumbers() {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(account_number, 4) AS INT)), 999) "
                + "FROM account";
        try (Connection con = DatabaseManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                Account.syncNextNumber(rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read account numbers: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Account a) {
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                Integer employerId = null;
                if (a instanceof ChequeAccount cheque) {
                    employerId = insertEmployer(con, cheque.getEmployer());
                }
                String sql = "INSERT INTO account (account_number, account_type, branch, "
                        + "date_opened, balance, customer_id, employer_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, a.getAccountNumber());
                    ps.setString(2, a.getAccountType());
                    ps.setString(3, a.getBranch());
                    ps.setDate(4, java.sql.Date.valueOf(a.getDateOpened()));
                    ps.setBigDecimal(5, a.getBalance());
                    ps.setString(6, a.getOwner().getCustomerId());
                    if (employerId == null) {
                        ps.setNull(7, Types.INTEGER);
                    } else {
                        ps.setInt(7, employerId);
                    }
                    ps.executeUpdate();
                }
                insertNewTransactions(con, a);
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save account: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ACCOUNT + "WHERE a.account_number = ?")) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Customer owner = customers.findById(rs.getString("customer_id")).orElse(null);
                return owner == null ? Optional.empty() : Optional.of(build(con, rs, owner));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read account: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findByCustomer(String customerId) {
        List<Account> result = new ArrayList<>();
        Customer owner = customers.findById(customerId).orElse(null);
        if (owner == null) {
            return result;
        }
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_ACCOUNT + "WHERE a.customer_id = ? ORDER BY a.account_number")) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(build(con, rs, owner));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read accounts: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Account> findAll() {
        List<Account> result = new ArrayList<>();
        Map<String, Customer> owners = new HashMap<>();
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ACCOUNT + "ORDER BY a.account_number");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String customerId = rs.getString("customer_id");
                Customer owner = owners.get(customerId);
                if (owner == null) {
                    owner = customers.findById(customerId).orElse(null);
                    owners.put(customerId, owner);
                }
                if (owner != null) {
                    result.add(build(con, rs, owner));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read accounts: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void update(Account a) {
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE account SET balance = ? WHERE account_number = ?")) {
                    ps.setBigDecimal(1, a.getBalance());
                    ps.setString(2, a.getAccountNumber());
                    ps.executeUpdate();
                }
                insertNewTransactions(con, a);
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update account: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String accountNumber) {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM account WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not delete account: " + e.getMessage(), e);
        }
    }

    // Helpers

    private int insertEmployer(Connection con, Employer e) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO employer (company_name, company_address) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getCompanyName());
            ps.setString(2, e.getCompanyAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Saves only the transactions that are not in the database yet. */
    private void insertNewTransactions(Connection con, Account a) throws SQLException {
        Set<String> existing = new HashSet<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT transaction_id FROM bank_transaction WHERE account_number = ?")) {
            ps.setString(1, a.getAccountNumber());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existing.add(rs.getString(1));
                }
            }
        }
        String sql = "INSERT INTO bank_transaction (transaction_id, account_number, "
                + "transaction_type, amount, balance_after, created_at, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Transaction t : a.getTransactions()) {
                if (existing.contains(t.getTransactionId())) {
                    continue;
                }
                ps.setString(1, t.getTransactionId());
                ps.setString(2, a.getAccountNumber());
                ps.setString(3, t.getType().name());
                ps.setBigDecimal(4, t.getAmount());
                ps.setBigDecimal(5, t.getBalanceAfter());
                ps.setTimestamp(6, Timestamp.valueOf(t.getTimestamp()));
                ps.setString(7, t.getNote());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Transaction> loadTransactions(Connection con, String accountNumber)
            throws SQLException {
        List<Transaction> history = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT transaction_id, transaction_type, amount, balance_after, created_at, note "
                        + "FROM bank_transaction WHERE account_number = ? ORDER BY created_at, transaction_id")) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new Transaction(
                            rs.getString("transaction_id"),
                            TransactionType.valueOf(rs.getString("transaction_type")),
                            rs.getBigDecimal("amount"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getBigDecimal("balance_after"),
                            rs.getString("note")));
                }
            }
        }
        return history;
    }

    /** Rebuilds an Account object of the right subclass from the current row. */
    private Account build(Connection con, ResultSet rs, Customer owner) throws SQLException {
        String number = rs.getString("account_number");
        String type = rs.getString("account_type");
        String branch = rs.getString("branch");
        java.time.LocalDate opened = rs.getDate("date_opened").toLocalDate();
        BigDecimal balance = rs.getBigDecimal("balance");
        String company = rs.getString("company_name");
        String companyAddress = rs.getString("company_address");
        List<Transaction> history = loadTransactions(con, number);

        return switch (type) {
            case "Savings" -> new SavingsAccount(owner, branch, number, opened, balance, history);
            case "Investment" -> new InvestmentAccount(owner, branch, number, opened, balance, history);
            case "Cheque" -> new ChequeAccount(owner, branch, new Employer(company, companyAddress),
                    number, opened, balance, history);
            default -> throw new IllegalStateException("Unknown account type: " + type);
        };
    }
}