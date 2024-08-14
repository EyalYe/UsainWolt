package Server.Models;

public class DeliveryUser extends User {
    private double income;
    private Order currentOrder;


    public DeliveryUser(String userName, String hashedPassword, String address, String phoneNumber, String email) {
        super(userName, hashedPassword, address, phoneNumber, email);
        this.income = 0;
    }

    public DeliveryUser(String csvLine) {
        super(csvLine);
        String[] fields = csvLine.split(",");
        this.income = Double.parseDouble(fields[8]);
    }

    @Override
    public void performUserSpecificAction() {

    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public void addIncome(double income) {
        this.income += income;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(Order currentOrder) {
        this.currentOrder = currentOrder;
    }

    @Override
    public String toString() {
        return super.toString() + "," + income;
    }

}
