// Group: 6
package Server.Models;

import Server.Models.Order;
import Server.Models.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerUser extends User {
    private List<Order> orderHistory; // List of orders placed by the customer
    private Map<String, String> preferences; // Customer preferences
    private CreditCard creditCard; // Customer's credit card information
    private double inAppBalance; // Customer's balance

    // Adds a specified amount to the in-app balance
    public void addBalance(double totalPrice) {
        inAppBalance += totalPrice;
    }

    // Removes an order from the order history based on order ID
    public void removeOrder(int orderId) {
        orderHistory.removeIf(order -> order.getOrderId() == orderId);
    }

    // Inner class for handling credit card information
    class CreditCard {
        private String creditCardNumber;
        private String expirationDate;
        private String cvv;

        // Constructor for initializing a credit card
        public CreditCard(String creditCardNumber, String expirationDate, String cvv) {
            this.creditCardNumber = creditCardNumber;
            this.expirationDate = expirationDate;
            this.cvv = cvv;
        }

        // Getters and setters for credit card attributes
        public String getCreditCardNumber() {
            return creditCardNumber;
        }

        public String getExpirationDate() {
            return expirationDate;
        }

        public String getCvv() {
            return cvv;
        }

        public void setCreditCardNumber(String creditCardNumber) {
            this.creditCardNumber = creditCardNumber;
        }

        public void setExpirationDate(String expirationDate) {
            this.expirationDate = expirationDate;
        }

        public void setCvv(String cvv) {
            this.cvv = cvv;
        }

        // Simulated payment methods
        public void makePayment(double amount) {
            System.out.println("Making payment of ₪" + amount + "...");
        }

        public void printCreditCardInfo() {
            System.out.println("Credit Card Number: " + creditCardNumber);
            System.out.println("Expiration Date: " + expirationDate);
            System.out.println("CVV: " + cvv);
        }

        public void printPaymentConfirmation(double amount) {
            System.out.println("Payment of ₪" + amount + " was successful!");
        }

        public void printPaymentError() {
            System.out.println("Payment failed. Please check your credit card information and try again.");
        }

        public void printInsufficientFundsError() {
            System.out.println("Payment failed due to insufficient funds. Please try again with a different card.");
        }

        public void printInvalidCardError() {
            System.out.println("Payment failed due to an invalid credit card. Please check your information and try again.");
        }
    }

    // Constructor for creating a new customer
    public CustomerUser(String userName, String hashedPassword, String address, String phoneNumber, String email) {
        super(userName, hashedPassword, address, phoneNumber, email);
        this.orderHistory = new ArrayList<>();
        this.preferences = new HashMap<>();
        this.creditCard = new CreditCard("", "", "");
        this.inAppBalance = 0.0;
    }

    // Constructor for creating a customer from a CSV line
    public CustomerUser(String csvLine) {
        super(csvLine);
        int index = 8;
        String[] fields = csvLine.split(",");
        this.orderHistory = new ArrayList<>();
        this.preferences = new HashMap<>();
        try {
            this.creditCard = new CreditCard(fields[index++], fields[index++], fields[index++]);
        } catch (ArrayIndexOutOfBoundsException e) {
            this.creditCard = new CreditCard("", "", "");
        }
        try{
        this.inAppBalance = fields[index].equals("") ? 0.0 : Double.parseDouble(fields[index]);
        } catch (ArrayIndexOutOfBoundsException e) {
            this.inAppBalance = 0.0;
        }
    }

    // Getters and Setters for order history and preferences
    public List<Order> getOrderHistory() {
        return new ArrayList<>(orderHistory);
    }

    public void setOrderHistory(List<Order> orderHistory) {
        this.orderHistory = new ArrayList<>(orderHistory);
    }

    public Map<String, String> getPreferences() {
        return new HashMap<>(preferences);
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = new HashMap<>(preferences);
    }

    @Override
    public void performUserSpecificAction() {
        System.out.println("Performing customer-specific action...");
    }

    // Additional methods for managing orders and preferences
    public void addOrder(Order order) {
        orderHistory.add(order);
    }

    public void updatePreference(String key, String value) {
        preferences.put(key, value);
    }

    // Getters and Setters for credit card information
    public String getCreditCardNumber() {
        return creditCard.getCreditCardNumber();
    }

    public void setCreditCardNumber(String creditCardNumber) {
        creditCard.setCreditCardNumber(creditCardNumber);
    }

    public String getExpirationDate() {
        return creditCard.getExpirationDate();
    }

    public void setExpirationDate(String expirationDate) {
        creditCard.setExpirationDate(expirationDate);
    }

    public String getCvv() {
        return creditCard.getCvv();
    }

    public void setCvv(String cvv) {
        creditCard.setCvv(cvv);
    }

    // Return a string representation of the customer with all relevant details
    @Override
    public String toString() {
        return "Customer," + super.toString() + "," + creditCard.getCreditCardNumber() + "," + creditCard.getExpirationDate() + "," + creditCard.getCvv() + "," + inAppBalance;
    }


}
