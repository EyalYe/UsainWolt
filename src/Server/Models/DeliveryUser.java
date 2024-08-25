package Server.Models;

public class DeliveryUser extends User {
    private double income;
    private Order currentOrder;

    // Constructor for creating a new delivery user with specified details
    public DeliveryUser(String userName, String hashedPassword, String address, String phoneNumber, String email) {
        super(userName, hashedPassword, address, phoneNumber, email);
        this.income = 0;
    }

    // Constructor for creating a delivery user from a CSV line
    public DeliveryUser(String csvLine) {
        super(csvLine);
        String[] fields = csvLine.split(",");
        this.income = Double.parseDouble(fields[8]);
    }

    @Override
    public void performUserSpecificAction() {

    }

    // Getters and Setters for income
    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    // Adds a specified amount to the delivery user's income
    public void addIncome(double income) {
        this.income += income;
    }

    // Getters and Setters for current order
    public Order getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(Order currentOrder) {
        this.currentOrder = currentOrder;
    }

    // Return a string representation of the delivery user with income
    @Override
    public String toString() {
        return super.toString() + "," + income;
    }

}
