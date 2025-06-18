// application/DatabaseManager.java
package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private Connection connection;

    public DatabaseManager() {
        try {
            // Get connection from DBConnection class
            connection = DBConnection.getConnection();
            if (connection == null) {
                // If getConnection returns null, it means the connection failed.
                // Throw an exception to stop further execution if connection is crucial.
                throw new SQLException("Failed to establish database connection. Check DBConnection.java logs for details.");
            }
            createTables(); // Ensure tables exist
        } catch (SQLException e) {
            System.err.println("DatabaseManager initialization failed: " + e.getMessage());
            e.printStackTrace();
            // In a real application, you might want to show an alert and exit
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create menu_items table
            String createMenuItemsTable = "CREATE TABLE IF NOT EXISTS menu_items ("
                                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                                        + "name VARCHAR(255) NOT NULL,"
                                        + "price DOUBLE NOT NULL"
                                        + ");";
            stmt.execute(createMenuItemsTable);

            // Create orders table
            String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders ("
                                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                                    + "table_number INT NOT NULL,"
                                    + "order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                                    + "total_amount DOUBLE NOT NULL,"
                                    + "status VARCHAR(50) DEFAULT 'pending' NOT NULL,"
                                    + "payment_status VARCHAR(50) DEFAULT 'pending' NOT NULL"
                                    + ");";
            stmt.execute(createOrdersTable);

            // Create order_items table (many-to-many relationship)
            String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items ("
                                         + "order_id INT NOT NULL,"
                                         + "menu_item_id INT NOT NULL,"
                                         + "quantity INT NOT NULL,"
                                         + "price_at_order DOUBLE NOT NULL,"
                                         + "PRIMARY KEY (order_id, menu_item_id),"
                                         + "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,"
                                         + "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE"
                                         + ");";
            stmt.execute(createOrderItemsTable);

            // Create table_bookings table
            String createTableBookingsTable = "CREATE TABLE IF NOT EXISTS table_bookings ("
                                            + "id INT AUTO_INCREMENT PRIMARY KEY,"
                                            + "table_number INT NOT NULL UNIQUE,"
                                            + "capacity INT NOT NULL,"
                                            + "customer_name VARCHAR(255),"
                                            + "booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                                            + ");";
            stmt.execute(createTableBookingsTable);

            System.out.println("Database tables checked/created successfully.");
        }
    }

    public void addDefaultMenuItems() {
        if (connection == null) {
            System.err.println("Cannot add default menu items: Database connection is null.");
            return; // Exit if no connection
        }
        try {
            // Check if menu_items table is empty
            String checkSql = "SELECT COUNT(*) FROM menu_items;";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next()) {
                    if (rs.getInt(1) == 0) { // Table is empty
                        System.out.println("Menu items table is empty. Adding default items...");
                        String insertSql = "INSERT INTO menu_items (name, price) VALUES (?, ?);";
                        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                            pstmt.setString(1, "Classic Burger");
                            pstmt.setDouble(2, 12.99);
                            pstmt.addBatch();

                            pstmt.setString(1, "Margherita Pizza");
                            pstmt.setDouble(2, 15.50);
                            pstmt.addBatch();

                            pstmt.setString(1, "Caesar Salad");
                            pstmt.setDouble(2, 9.75);
                            pstmt.addBatch();

                            pstmt.setString(1, "French Fries");
                            pstmt.setDouble(2, 4.00);
                            pstmt.addBatch();

                            pstmt.setString(1, "Coca-Cola");
                            pstmt.setDouble(2, 2.50);
                            pstmt.addBatch();

                            pstmt.executeBatch();
                            System.out.println("Default menu items added successfully.");
                        }
                    } else {
                        System.out.println("Menu items table already contains data. Skipping default item insertion.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding default menu items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addMenuItem(MenuItem item) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "INSERT INTO menu_items (name, price) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.executeUpdate();

            // Get the generated ID and set it back to the MenuItem object
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<MenuItem> loadMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        if (connection == null) {
            System.err.println("Cannot load menu items: Database connection is null.");
            return menuItems;
        }
        String sql = "SELECT id, name, price FROM menu_items ORDER BY id ASC;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                menuItems.add(new MenuItem(id, name, price));
            }
            System.out.println("Menu items loaded from database. Next available ID: " + (menuItems.isEmpty() ? 1 : menuItems.get(menuItems.size() - 1).getId() + 1));
        } catch (SQLException e) {
            System.err.println("Error loading menu items: " + e.getMessage());
            e.printStackTrace();
        }
        return menuItems;
    }

    public void updateMenuItem(MenuItem item) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "UPDATE menu_items SET name = ?, price = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteMenuItem(int id) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "DELETE FROM menu_items WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // --- Order related methods ---
    public int createOrder(int tableNumber, double totalAmount) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "INSERT INTO orders (table_number, total_amount, status, payment_status) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, tableNumber);
            pstmt.setDouble(2, totalAmount);
            pstmt.setString(3, "pending"); // Default status for new orders
            pstmt.setString(4, "pending"); // Default payment status for new orders
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Return the generated order ID
                }
            }
        }
        return -1; // Indicate failure
    }

    public void addOrderItem(int orderId, int menuItemId, int quantity, double priceAtOrder) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price_at_order) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, menuItemId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, priceAtOrder);
            pstmt.executeUpdate();
        }
    }

    public List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();
        if (connection == null) {
            System.err.println("Cannot load orders: Database connection is null.");
            return orders;
        }
        String sql = "SELECT o.id, o.table_number, o.order_time, o.total_amount, o.status, o.payment_status " +
                     "FROM orders o ORDER BY o.order_time DESC;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int orderId = rs.getInt("id");
                int tableNumber = rs.getInt("table_number");
                Timestamp orderTime = rs.getTimestamp("order_time");
                double totalAmount = rs.getDouble("total_amount");
                String status = rs.getString("status");
                String paymentStatus = rs.getString("payment_status");
                Order order = new Order(orderId, tableNumber, orderTime, totalAmount, status, paymentStatus);
                order.setItems(loadOrderItemsForOrder(orderId)); // Load associated items
                orders.add(order);
            }
            System.out.println("Orders loaded from DB. Total: " + orders.size());
        } catch (SQLException e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    public List<OrderItem> loadOrderItemsForOrder(int orderId) throws SQLException {
        List<OrderItem> orderItems = new ArrayList<>();
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "SELECT oi.menu_item_id, oi.quantity, oi.price_at_order, mi.name " +
                     "FROM order_items oi JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                     "WHERE oi.order_id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int menuItemId = rs.getInt("menu_item_id");
                    int quantity = rs.getInt("quantity");
                    double priceAtOrder = rs.getDouble("price_at_order");
                    String itemName = rs.getString("name");
                    orderItems.add(new OrderItem(menuItemId, itemName, quantity, priceAtOrder));
                }
            }
        }
        return orderItems;
    }

    public void updateOrderStatus(int orderId, String newStatus) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "UPDATE orders SET status = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        }
    }

    public void updateOrderPaymentStatus(int orderId, String newPaymentStatus) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "UPDATE orders SET payment_status = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPaymentStatus);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        }
    }


    // --- Table Booking related methods ---
    public void addTableBooking(TableBooking booking) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "INSERT INTO table_bookings (table_number, capacity, customer_name, booking_time) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, booking.getTableNumber());
            pstmt.setInt(2, booking.getCapacity());
            pstmt.setString(3, booking.getCustomerName());
            pstmt.setTimestamp(4, booking.getBookingTime());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    booking.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<TableBooking> loadTableBookings() {
        List<TableBooking> bookings = new ArrayList<>();
        if (connection == null) {
            System.err.println("Cannot load table bookings: Database connection is null.");
            return bookings;
        }
        String sql = "SELECT id, table_number, capacity, customer_name, booking_time FROM table_bookings;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int tableNumber = rs.getInt("table_number");
                int capacity = rs.getInt("capacity");
                String customerName = rs.getString("customer_name");
                Timestamp bookingTime = rs.getTimestamp("booking_time");
                bookings.add(new TableBooking(id, tableNumber, capacity, customerName, bookingTime));
            }
            System.out.println("Table bookings loaded from DB. Total: " + bookings.size());
        } catch (SQLException e) {
            System.err.println("Error loading table bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    public void updateTableBooking(TableBooking booking) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "UPDATE table_bookings SET table_number = ?, capacity = ?, customer_name = ?, booking_time = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, booking.getTableNumber());
            pstmt.setInt(2, booking.getCapacity());
            pstmt.setString(3, booking.getCustomerName());
            pstmt.setTimestamp(4, booking.getBookingTime());
            pstmt.setInt(5, booking.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteTableBooking(int id) throws SQLException {
        if (connection == null) throw new SQLException("Database connection is null.");
        String sql = "DELETE FROM table_bookings WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void close() {
        DBConnection.closeConnection(this.connection);
    }
}
