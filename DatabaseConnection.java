import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // JDBC URL, username, and password of MySQL server
    private static final String URL = "jdbc:mysql://localhost:3306/agotsrestaurant";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "Hernandez14"; // Replace with your MySQL password

    // JDBC Driver
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Establishes a connection to the database.
     *
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load JDBC driver
            Class.forName(DRIVER);
            // Return the database connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver not found! Ensure MySQL Connector/J is in the classpath.");
            e.printStackTrace();
            throw new SQLException("Database driver not found.", e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database! Check your credentials and database status.");
            throw e;
        }
    }

    /**
     * Closes the database connection.
     *
     * @param connection the Connection object to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                System.err.println("Error while closing the connection!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Test the database connection.
     */
    public static void main(String[] args) {
        Connection connection = null;
        try {
            connection = getConnection();
            System.out.println("Database connection test successful.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }
}
