// application/Main.java
package application;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty; // Import this
import javafx.beans.property.SimpleIntegerProperty; // Import this
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;  // Import this
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
// import javafx.scene.control.cell.PropertyValueFactory; // REMOVE THIS IMPORT
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class Main extends Application {

    private DatabaseManager dbManager;

    // ObservableLists for TableViews
    private ObservableList<MenuItem> menuItems;
    private ObservableList<Order> orders;
    private ObservableList<TableBooking> tableBookings;

    // UI elements for Menu Management
    private TextField menuItemNameField;
    private TextField menuItemPriceField;
    private TableView<MenuItem> menuTable;

    // UI elements for Order Management (simplified for now)
    private TableView<Order> orderTable;
    private TextField orderTableNumberField;
    private TableView<OrderItem> currentOrderItemsTable; // For adding items to a new order
    private ObservableList<OrderItem> currentOrderItems; // List for items in current order being placed

    // UI elements for Table Booking Management
    private TextField bookingTableNumberField;
    private TextField bookingCapacityField;
    private TextField bookingCustomerNameField;
    private TableView<TableBooking> bookingTable;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        dbManager = new DatabaseManager();

        // Only proceed if database connection was successful
        if (dbManager.getConnection() == null) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Database Connection Failed",
                      "The application could not connect to the database. Please check your database server and credentials.");
            primaryStage.close(); // Close the application if DB connection fails
            return;
        }

        dbManager.addDefaultMenuItems(); // Add default menu items if table is empty

        // Initialize ObservableLists
        menuItems = FXCollections.observableArrayList();
        orders = FXCollections.observableArrayList();
        tableBookings = FXCollections.observableArrayList();
        currentOrderItems = FXCollections.observableArrayList(); // For new order creation

        // Load data from DB initially
        loadAllData();

        primaryStage.setTitle("Restaurant Management System");

        TabPane tabPane = new TabPane();

        // --- Menu Tab ---
        Tab menuTab = new Tab("Menu Management");
        menuTab.setClosable(false);
        menuTab.setContent(createMenuTab());
        tabPane.getTabs().add(menuTab);

        // --- Order Tab ---
        Tab orderTab = new Tab("Order Management");
        orderTab.setClosable(false);
        orderTab.setContent(createOrderTab());
        tabPane.getTabs().add(orderTab);

        // --- Table Booking Tab ---
        Tab bookingTab = new Tab("Table Booking");
        bookingTab.setClosable(false);
        bookingTab.setContent(createBookingTab());
        tabPane.getTabs().add(bookingTab);

        Scene scene = new Scene(tabPane, 1200, 700); // Adjust size as needed
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadAllData() {
        // Only load if dbManager is properly initialized and connected
        if (dbManager != null && dbManager.getConnection() != null) {
            menuItems.setAll(dbManager.loadMenuItems());
            System.out.println("Menu items reloaded from DB. Total: " + menuItems.size());

            orders.setAll(dbManager.loadOrders());
            System.out.println("Orders loaded from DB. Total: " + orders.size());

            tableBookings.setAll(dbManager.loadTableBookings());
            System.out.println("Table bookings loaded from DB. Total: " + tableBookings.size());
        } else {
            System.err.println("Database manager not initialized or connected. Cannot load data.");
        }
    }

    private VBox createMenuTab() {
        VBox menuTabContent = new VBox(10);
        menuTabContent.setPadding(new Insets(10));

        // Form for adding/updating menu items
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER_LEFT);

        menuItemNameField = new TextField();
        menuItemNameField.setPromptText("Menu Item Name");
        menuItemPriceField = new TextField();
        menuItemPriceField.setPromptText("Price");

        Button addButton = new Button("Add Item");
        addButton.setOnAction(e -> addMenuItem());
        Button updateButton = new Button("Update Item");
        updateButton.setOnAction(e -> updateMenuItem());
        Button deleteButton = new Button("Delete Item");
        deleteButton.setOnAction(e -> deleteMenuItem());

        formGrid.addRow(0, new Label("Name:"), menuItemNameField);
        formGrid.addRow(1, new Label("Price:"), menuItemPriceField);
        HBox buttonBox = new HBox(10, addButton, updateButton, deleteButton);
        formGrid.addRow(2, buttonBox);

        // Table for displaying menu items
        menuTable = new TableView<>();
        TableColumn<MenuItem, Integer> idCol = new TableColumn<>("ID");
        // CHANGED: Using lambda for cellValueFactory instead of PropertyValueFactory
        idCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        
        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        // CHANGED: Using lambda for cellValueFactory instead of PropertyValueFactory
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        
        TableColumn<MenuItem, Double> priceCol = new TableColumn<>("Price");
        // CHANGED: Using lambda for cellValueFactory instead of PropertyValueFactory
        priceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());

        menuTable.getColumns().addAll(idCol, nameCol, priceCol);
        menuTable.setItems(menuItems);

        // Listener for selecting items in the table
        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                menuItemNameField.setText(newSelection.getName());
                menuItemPriceField.setText(String.valueOf(newSelection.getPrice()));
            } else {
                menuItemNameField.clear();
                menuItemPriceField.clear();
            }
        });

        menuTabContent.getChildren().addAll(formGrid, new Label("Available Menu Items:"), menuTable);
        return menuTabContent;
    }

    private void addMenuItem() {
        try {
            String name = menuItemNameField.getText();
            double price = Double.parseDouble(menuItemPriceField.getText());
            MenuItem newItem = new MenuItem(0, name, price); // ID will be set by DB

            dbManager.addMenuItem(newItem);
            menuItems.add(newItem); // Add with the DB-generated ID
            clearMenuItemFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Added", "Menu item '" + name + "' has been added.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Please enter a valid number for price.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to Add Menu Item", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMenuItem() {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                String name = menuItemNameField.getText();
                double price = Double.parseDouble(menuItemPriceField.getText());

                selectedItem.setName(name);
                selectedItem.setPrice(price);

                dbManager.updateMenuItem(selectedItem);
                menuTable.refresh(); // Refresh the table to show updated data
                clearMenuItemFields();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Updated", "Menu item '" + name + "' has been updated.");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Please enter a valid number for price.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to Update Menu Item", "Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Menu Item Selected", "Please select a menu item to update.");
        }
    }

    private void deleteMenuItem() {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Menu Item: " + selectedItem.getName());
            alert.setContentText("Are you sure you want to delete this menu item?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    dbManager.deleteMenuItem(selectedItem.getId());
                    menuItems.remove(selectedItem);
                    clearMenuItemFields();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Deleted", "Menu item '" + selectedItem.getName() + "' has been deleted.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to Delete Menu Item", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Menu Item Selected", "Please select a menu item to delete.");
        }
    }

    private void clearMenuItemFields() {
        menuItemNameField.clear();
        menuItemPriceField.clear();
        menuTable.getSelectionModel().clearSelection();
    }


    private VBox createOrderTab() {
        VBox orderTabContent = new VBox(10);
        orderTabContent.setPadding(new Insets(10));

        // --- Create New Order Section ---
        Label newOrderLabel = new Label("Create New Order:");
        HBox newOrderInputs = new HBox(10);
        orderTableNumberField = new TextField();
        orderTableNumberField.setPromptText("Table Number");
        Button createOrderButton = new Button("Place Order");
        createOrderButton.setOnAction(e -> placeNewOrder());
        newOrderInputs.getChildren().addAll(new Label("Table:"), orderTableNumberField, createOrderButton);

        // Menu items for selection (from menuItems list)
        Label selectItemsLabel = new Label("Select Items for Order:");
        TableView<MenuItem> orderMenuItemSelectionTable = new TableView<>();
        TableColumn<MenuItem, String> itemSelectNameCol = new TableColumn<>("Item");
        // CHANGED: Using lambda for cellValueFactory
        itemSelectNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        
        TableColumn<MenuItem, Double> itemSelectPriceCol = new TableColumn<>("Price");
        // CHANGED: Using lambda for cellValueFactory
        itemSelectPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());

        orderMenuItemSelectionTable.getColumns().addAll(itemSelectNameCol, itemSelectPriceCol);
        orderMenuItemSelectionTable.setItems(menuItems); // Use the existing menuItems list

        HBox addRemoveItemBox = new HBox(10);
        Button addItemToOrderButton = new Button("Add to Current Order");
        TextField quantityField = new TextField("1");
        quantityField.setPrefColumnCount(3);
        addItemToOrderButton.setOnAction(e -> addItemToCurrentOrder(orderMenuItemSelectionTable, quantityField));
        addRemoveItemBox.getChildren().addAll(addItemToOrderButton, new Label("Qty:"), quantityField);

        // Current Order Items Table
        Label currentOrderItemsLabel = new Label("Current Order Details:");
        currentOrderItemsTable = new TableView<>();
        TableColumn<OrderItem, String> currentItemNameCol = new TableColumn<>("Item");
        // CHANGED: Using lambda for cellValueFactory
        currentItemNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemName()));
        
        TableColumn<OrderItem, Integer> currentItemQtyCol = new TableColumn<>("Quantity");
        // CHANGED: Using lambda for cellValueFactory
        currentItemQtyCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        
        TableColumn<OrderItem, Double> currentItemPriceCol = new TableColumn<>("Price Each");
        // CHANGED: Using lambda for cellValueFactory
        currentItemPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPriceAtOrder()).asObject());
        
        TableColumn<OrderItem, Double> currentItemTotalCol = new TableColumn<>("Subtotal");
        // This was already a lambda, so it's fine.
        currentItemTotalCol.setCellValueFactory(cellData -> {
            OrderItem item = cellData.getValue();
            return new SimpleDoubleProperty(item.getQuantity() * item.getPriceAtOrder()).asObject();
        });
        currentOrderItemsTable.getColumns().addAll(currentItemNameCol, currentItemQtyCol, currentItemPriceCol, currentItemTotalCol);
        currentOrderItemsTable.setItems(currentOrderItems);


        // --- Existing Orders Section ---
        Label existingOrdersLabel = new Label("Existing Orders:");
        orderTable = new TableView<>();
        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        // CHANGED: Using lambda for cellValueFactory
        orderIdCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        
        TableColumn<Order, Integer> orderTableCol = new TableColumn<>("Table No.");
        // CHANGED: Using lambda for cellValueFactory
        orderTableCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTableNumber()).asObject());
        
        TableColumn<Order, Timestamp> orderTimeCol = new TableColumn<>("Order Time");
        // CHANGED: Using lambda for cellValueFactory (Timestamp is an Object, so no Simple*Property needed, but it works fine with it too)
        orderTimeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderTime()));
        
        TableColumn<Order, Double> orderTotalCol = new TableColumn<>("Total Amount");
        // CHANGED: Using lambda for cellValueFactory
        orderTotalCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
        
        TableColumn<Order, String> orderStatusCol = new TableColumn<>("Status"); // Display status
        // CHANGED: Using lambda for cellValueFactory
        orderStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        
        TableColumn<Order, String> orderPaymentStatusCol = new TableColumn<>("Payment"); // Display payment status
        // CHANGED: Using lambda for cellValueFactory
        orderPaymentStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatus()));


        orderTable.getColumns().addAll(orderIdCol, orderTableCol, orderTimeCol, orderTotalCol, orderStatusCol, orderPaymentStatusCol);
        orderTable.setItems(orders);

        // Order details expansion
        orderTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    VBox detailsBox = new VBox(5);
                    detailsBox.setPadding(new Insets(5, 0, 5, 10));
                    Label itemsLabel = new Label("Items:");
                    detailsBox.getChildren().add(itemsLabel);

                    for (OrderItem oi : newItem.getItems()) {
                        detailsBox.getChildren().add(new Label(String.format("  - %s (x%d) @ %.2f = %.2f",
                                oi.getItemName(), oi.getQuantity(), oi.getPriceAtOrder(), oi.getQuantity() * oi.getPriceAtOrder())));
                    }

                    // Status update buttons
                    HBox statusButtons = new HBox(10);
                    Button prepareBtn = new Button("Set Preparing");
                    prepareBtn.setOnAction(e -> updateOrderStatus(newItem, "preparing"));
                    Button serveBtn = new Button("Set Served");
                    serveBtn.setOnAction(e -> updateOrderStatus(newItem, "served"));
                    Button cancelBtn = new Button("Set Cancelled");
                    cancelBtn.setOnAction(e -> updateOrderStatus(newItem, "cancelled"));

                    Button payBtn = new Button("Mark Paid");
                    payBtn.setOnAction(e -> updateOrderPaymentStatus(newItem, "paid"));
                    Button refundBtn = new Button("Mark Refunded");
                    refundBtn.setOnAction(e -> updateOrderPaymentStatus(newItem, "refunded"));

                    statusButtons.getChildren().addAll(prepareBtn, serveBtn, cancelBtn, new Separator(), payBtn, refundBtn);
                    detailsBox.getChildren().add(statusButtons);
                    row.setGraphic(detailsBox); // This might be better as a separate details pane or popup
                }
            });
            return row;
        });


        // Add all to the tab content
        orderTabContent.getChildren().addAll(newOrderLabel, newOrderInputs, selectItemsLabel, orderMenuItemSelectionTable,
                                            addRemoveItemBox, currentOrderItemsLabel, currentOrderItemsTable,
                                            existingOrdersLabel, orderTable);
        return orderTabContent;
    }


    private void addItemToCurrentOrder(TableView<MenuItem> itemSelectionTable, TextField quantityField) {
        MenuItem selectedMenuItem = itemSelectionTable.getSelectionModel().getSelectedItem();
        if (selectedMenuItem != null) {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Quantity must be positive.", "Please enter a quantity greater than zero.");
                    return;
                }

                // Check if item already exists in current order items
                Optional<OrderItem> existingOrderItem = currentOrderItems.stream()
                    .filter(oi -> oi.getMenuItemId() == selectedMenuItem.getId())
                    .findFirst();

                if (existingOrderItem.isPresent()) {
                    // Update quantity if item already exists
                    OrderItem oi = existingOrderItem.get();
                    oi.setQuantity(oi.getQuantity() + quantity);
                    currentOrderItemsTable.refresh(); // Refresh the table to show updated quantity
                } else {
                    // Add new item to the list
                    currentOrderItems.add(new OrderItem(selectedMenuItem.getId(), selectedMenuItem.getName(), quantity, selectedMenuItem.getPrice()));
                }
                quantityField.setText("1"); // Reset quantity field
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity Error", "Please enter a valid number for quantity.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Menu Item Selected", "Please select an item from the menu to add to the order.");
        }
    }


    private void placeNewOrder() {
        if (currentOrderItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Order", "No Items in Order", "Please add items to the order before placing.");
            return;
        }

        try {
            int tableNumber = Integer.parseInt(orderTableNumberField.getText());
            if (tableNumber <= 0) {
                 showAlert(Alert.AlertType.WARNING, "Invalid Table Number", "Table number must be positive.", "Please enter a valid table number.");
                 return;
            }

            double totalAmount = currentOrderItems.stream()
                                                .mapToDouble(item -> item.getQuantity() * item.getPriceAtOrder())
                                                .sum();

            int orderId = dbManager.createOrder(tableNumber, totalAmount);
            if (orderId != -1) {
                for (OrderItem item : currentOrderItems) {
                    dbManager.addOrderItem(orderId, item.getMenuItemId(), item.getQuantity(), item.getPriceAtOrder());
                }

                showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Order #" + orderId + " Placed", "Order for table " + tableNumber + " placed successfully!");
                clearNewOrderFields();
                loadAllData(); // Reload all data to refresh tables
            } else {
                showAlert(Alert.AlertType.ERROR, "Order Error", "Failed to Place Order", "Could not create order in the database.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Table Number Error", "Please enter a valid table number.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Order Placement Failed", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearNewOrderFields() {
        orderTableNumberField.clear();
        currentOrderItems.clear(); // Clear the items from the current order builder
    }

    private void updateOrderStatus(Order order, String newStatus) {
        try {
            dbManager.updateOrderStatus(order.getId(), newStatus);
            order.setStatus(newStatus); // Update the ObservableList item directly
            orderTable.refresh(); // Refresh the table view
            showAlert(Alert.AlertType.INFORMATION, "Status Updated", "Order #" + order.getId() + " Status", "Order status updated to: " + newStatus);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Status Update Failed", "Error updating order status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateOrderPaymentStatus(Order order, String newPaymentStatus) {
        try {
            dbManager.updateOrderPaymentStatus(order.getId(), newPaymentStatus);
            order.setPaymentStatus(newPaymentStatus); // Update the ObservableList item directly
            orderTable.refresh(); // Refresh the table view
            showAlert(Alert.AlertType.INFORMATION, "Payment Status Updated", "Order #" + order.getId() + " Payment Status", "Order payment status updated to: " + newPaymentStatus);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Payment Status Update Failed", "Error updating order payment status: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private VBox createBookingTab() {
        VBox bookingTabContent = new VBox(10);
        bookingTabContent.setPadding(new Insets(10));

        // Form for adding/updating bookings
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER_LEFT);

        bookingTableNumberField = new TextField();
        bookingTableNumberField.setPromptText("Table Number");
        bookingCapacityField = new TextField();
        bookingCapacityField.setPromptText("Capacity");
        bookingCustomerNameField = new TextField();
        bookingCustomerNameField.setPromptText("Customer Name");

        Button addBookingButton = new Button("Add Booking");
        addBookingButton.setOnAction(e -> addBooking());
        Button updateBookingButton = new Button("Update Booking");
        updateBookingButton.setOnAction(e -> updateBooking());
        Button deleteBookingButton = new Button("Delete Booking");
        deleteBookingButton.setOnAction(e -> deleteBooking());

        formGrid.addRow(0, new Label("Table No:"), bookingTableNumberField);
        formGrid.addRow(1, new Label("Capacity:"), bookingCapacityField);
        formGrid.addRow(2, new Label("Customer:"), bookingCustomerNameField);
        HBox buttonBox = new HBox(10, addBookingButton, updateBookingButton, deleteBookingButton);
        formGrid.addRow(3, buttonBox);

        // Table for displaying bookings
        bookingTable = new TableView<>();
        TableColumn<TableBooking, Integer> bookingIdCol = new TableColumn<>("ID");
        // CHANGED: Using lambda for cellValueFactory
        bookingIdCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        
        TableColumn<TableBooking, Integer> bookingTableNoCol = new TableColumn<>("Table No.");
        // CHANGED: Using lambda for cellValueFactory
        bookingTableNoCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTableNumber()).asObject());
        
        TableColumn<TableBooking, Integer> bookingCapacityCol = new TableColumn<>("Capacity");
        // CHANGED: Using lambda for cellValueFactory
        bookingCapacityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCapacity()).asObject());
        
        TableColumn<TableBooking, String> bookingCustomerNameCol = new TableColumn<>("Customer Name");
        // CHANGED: Using lambda for cellValueFactory
        bookingCustomerNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        
        TableColumn<TableBooking, Timestamp> bookingTimeCol = new TableColumn<>("Booking Time");
        // CHANGED: Using lambda for cellValueFactory
        bookingTimeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBookingTime()));

        bookingTable.getColumns().addAll(bookingIdCol, bookingTableNoCol, bookingCapacityCol, bookingCustomerNameCol, bookingTimeCol);
        bookingTable.setItems(tableBookings);

        // Listener for selecting items in the table
        bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                bookingTableNumberField.setText(String.valueOf(newSelection.getTableNumber()));
                bookingCapacityField.setText(String.valueOf(newSelection.getCapacity()));
                bookingCustomerNameField.setText(newSelection.getCustomerName());
                // Booking time might be trickier to display/edit directly unless using a DateTimePicker
            } else {
                clearBookingFields();
            }
        });

        bookingTabContent.getChildren().addAll(formGrid, new Label("Table Bookings:"), bookingTable);
        return bookingTabContent;
    }

    private void addBooking() {
        try {
            int tableNumber = Integer.parseInt(bookingTableNumberField.getText());
            int capacity = Integer.parseInt(bookingCapacityField.getText());
            String customerName = bookingCustomerNameField.getText();
            Timestamp bookingTime = Timestamp.valueOf(LocalDateTime.now()); // Set current time as booking time

            TableBooking newBooking = new TableBooking(0, tableNumber, capacity, customerName, bookingTime);
            dbManager.addTableBooking(newBooking);
            tableBookings.add(newBooking); // Add with DB-generated ID
            clearBookingFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Table Booking Added", "Booking for table " + tableNumber + " for " + customerName + " has been added.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Please enter valid numbers for table number and capacity.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to Add Table Booking", "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBooking() {
        TableBooking selectedBooking = bookingTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            try {
                int tableNumber = Integer.parseInt(bookingTableNumberField.getText());
                int capacity = Integer.parseInt(bookingCapacityField.getText());
                String customerName = bookingCustomerNameField.getText();

                selectedBooking.setTableNumber(tableNumber);
                selectedBooking.setCapacity(capacity);
                selectedBooking.setCustomerName(customerName);
                // Booking time is not updated via text field in this example

                dbManager.updateTableBooking(selectedBooking);
                bookingTable.refresh();
                clearBookingFields();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Table Booking Updated", "Booking for table " + tableNumber + " has been updated.");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Please enter valid numbers for table number and capacity.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to Update Table Booking", "Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Booking Selected", "Please select a table booking to update.");
        }
    }

    private void deleteBooking() {
        TableBooking selectedBooking = bookingTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Booking for: " + selectedBooking.getCustomerName() + " (Table " + selectedBooking.getTableNumber() + ")");
            alert.setContentText("Are you sure you want to delete this table booking?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    dbManager.deleteTableBooking(selectedBooking.getId());
                    tableBookings.remove(selectedBooking);
                    clearBookingFields();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Table Booking Deleted", "Booking has been deleted.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to Delete Table Booking", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Booking Selected", "Please select a table booking to delete.");
        }
    }

    private void clearBookingFields() {
        bookingTableNumberField.clear();
        bookingCapacityField.clear();
        bookingCustomerNameField.clear();
        bookingTable.getSelectionModel().clearSelection();
    }


    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
}
