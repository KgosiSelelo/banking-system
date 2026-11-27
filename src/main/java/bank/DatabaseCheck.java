package bank;

import bank.dao.DatabaseManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseCheck {

    public static void main(String[] args) throws Exception {
        DatabaseManager.initialise();
        try (Connection con = DatabaseManager.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT table_name FROM information_schema.tables "
                             + "WHERE table_schema = 'PUBLIC' ORDER BY table_name")) {
            System.out.println("Tables created:");
            while (rs.next()) {
                System.out.println("  " + rs.getString(1));
            }
        }
    }
}