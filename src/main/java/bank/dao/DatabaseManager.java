package bank.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens connections to the H2 database and creates the tables on first run. */
public final class DatabaseManager {

    private static final String URL = "jdbc:h2:file:./data/bankdb";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private DatabaseManager() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Runs schema.sql. Safe to call every time because it uses CREATE IF NOT EXISTS. */
    public static void initialise() {
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("schema.sql not found in resources");
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection con = getConnection(); Statement st = con.createStatement()) {
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) {
                        st.execute(sql.trim());
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Could not set up the database", e);
        }
    }
}