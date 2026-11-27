package bank.dao;

import bank.model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores customers in the database using JDBC. */
public class JdbcCustomerDAO implements CustomerDAO {

    private static final String COLUMNS =
            "customer_id, first_name, surname, address, username, salt, password_hash";

    @Override
    public void save(Customer c) {
        String sql = "INSERT INTO customer (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCustomerId());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getSurname());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getUsername());
            ps.setString(6, c.getSalt());
            ps.setString(7, c.getPasswordHash());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Customer> findById(String customerId) {
        return findOne("SELECT " + COLUMNS + " FROM customer WHERE customer_id = ?", customerId);
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        return findOne("SELECT " + COLUMNS + " FROM customer WHERE LOWER(username) = LOWER(?)",
                username);
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> result = new ArrayList<>();
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT " + COLUMNS + " FROM customer ORDER BY customer_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read customers: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void update(Customer c) {
        String sql = "UPDATE customer SET first_name = ?, surname = ?, address = ? "
                + "WHERE customer_id = ?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getSurname());
            ps.setString(3, c.getAddress());
            ps.setString(4, c.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update customer: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String customerId) {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM customer WHERE customer_id = ?")) {
            ps.setString(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not delete customer: " + e.getMessage(), e);
        }
    }

    private Optional<Customer> findOne(String sql, String value) {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read customer: " + e.getMessage(), e);
        }
    }

    private static Customer map(ResultSet rs) throws SQLException {
        return Customer.restore(
                rs.getString("customer_id"), rs.getString("first_name"),
                rs.getString("surname"), rs.getString("address"),
                rs.getString("username"), rs.getString("salt"),
                rs.getString("password_hash"));
    }
}