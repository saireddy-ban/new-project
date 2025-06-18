// application/TableBooking.java
package application;

import java.sql.Timestamp;

public class TableBooking {
    private int id;
    private int tableNumber;
    private int capacity;
    private String customerName;
    private Timestamp bookingTime;

    public TableBooking(int id, int tableNumber, int capacity, String customerName, Timestamp bookingTime) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.customerName = customerName;
        this.bookingTime = bookingTime;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Timestamp getBookingTime() {
        return bookingTime;
    }

    // Setters (if values can be updated after creation)
    public void setId(int id) {
        this.id = id;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setBookingTime(Timestamp bookingTime) {
        this.bookingTime = bookingTime;
    }

    @Override
    public String toString() {
        return "TableBooking [id=" + id + ", tableNumber=" + tableNumber + ", capacity=" + capacity + ", customerName=" + customerName + ", bookingTime=" + bookingTime + "]";
    }
}
