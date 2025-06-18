// application/Order.java
package application;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int tableNumber;
    private Timestamp orderTime;
    private double totalAmount;
    private String status; // Added status field
    private String paymentStatus; // Added paymentStatus field
    private List<OrderItem> items; // List of items in this order

    public Order(int id, int tableNumber, Timestamp orderTime, double totalAmount, String status, String paymentStatus) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.items = new ArrayList<>();
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    // Setters (if values can be updated after creation)
    public void setId(int id) {
        this.id = id;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addOrderItem(OrderItem item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Order [id=" + id + ", tableNumber=" + tableNumber + ", orderTime=" + orderTime + ", totalAmount=" + totalAmount + ", status=" + status + ", paymentStatus=" + paymentStatus + ", items=" + items.size() + " items]";
    }
}
