package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database credentials - *CHANGE THESE TO YOUR ACTUAL DATABASE DETAILS*
    private static final String URL = "jdbc:mysql://localhost:3306/student";
    private static final String USER = "root"; // e.g., "root" or your custom user
    private static final String PASSWORD = "Mani1107"; // e.g., "root" or your custom password

    // Static block to load the JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Make sure mysql-connector-java is in your classpath.");
            e.printStackTrace();
            System.exit(1); // Exit if driver is not found
        }
    }

    /**
     * Establishes a connection to the database.
     * @return A valid Connection object, or null if connection fails.
     */
    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established.");
            return connection;
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            // In a real application, you might want to show an alert to the user here.
            return null;
        }
    }

    /**
     * Closes the database connection.
     * @param connection The Connection object to close.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}
