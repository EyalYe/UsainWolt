package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class CustomerUser extends User {
    private List<Order> orderHistory;
    private Map<String, String> preferences;
    private CreditCard creditCard;

    public String getCreditCardNumber() {
        return creditCard.getCreditCardNumber();
    }

    public String getExpirationDate() {
        return creditCard.getExpirationDate();
    }

    public String getCvv() {
        return creditCard.getCvv();
    }

    class CreditCard {
        private String creditCardNumber;
        private String expirationDate;
        private String cvv;


        public CreditCard(String creditCardNumber, String expirationDate, String cvv) {
            this.creditCardNumber = creditCardNumber;
            this.expirationDate = expirationDate;
            this.cvv = cvv;
        }

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


        public void makePayment(double amount) {
            // Implementation for making a payment
            System.out.println("Making payment of $" + amount + "...");
        }

        public void printCreditCardInfo() {
            System.out.println("Credit Card Number: " + creditCardNumber);
            System.out.println("Expiration Date: " + expirationDate);
            System.out.println("CVV: " + cvv);
        }

        public void printPaymentConfirmation(double amount) {
            System.out.println("Payment of $" + amount + " was successful!");
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

    public CustomerUser(String userName, String hashedPassword, String address, String phoneNumber, String email) {
        super(userName, hashedPassword, address, phoneNumber, email);
        this.orderHistory = new ArrayList<>();
        this.preferences = new HashMap<>();
        this.creditCard = new CreditCard("", "", "");
    }

    // Getters and Setters

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
        // Implementation for customer-specific action
        System.out.println("Performing customer-specific action...");
    }

    // Additional methods specific to CustomerUser

    public void addOrder(Order order) {
        orderHistory.add(order);
    }

    public void updatePreference(String key, String value) {
        preferences.put(key, value);
    }

    // Main method for testing purposes
    public static void main(String[] args) {
        CustomerUser customer = new CustomerUser("john_doe", "hashed_password", "123 Main St", "555-1234", "john@example.com");

        // Update preferences
        customer.updatePreference("cuisine", "Italian");
        customer.updatePreference("distance", "5km");

        // Create an order and add it to the customer's order history
        Order order = new Order(1, new Date(), List.of(new Order.Item("Pizza", 12.99)), "John Doe", "Fast Food Inc.", "Pending", "Extra cheese");
        customer.addOrder(order);

        customer.performUserSpecificAction();
        System.out.println("Order History: " + customer.getOrderHistory());
        System.out.println("Preferences: " + customer.getPreferences());
    }

    public void setCreditCardNumber(String creditCardNumber) {
        creditCard.setCreditCardNumber(creditCardNumber);
    }

    public void setExpirationDate(String expirationDate) {
        creditCard.setExpirationDate(expirationDate);
    }

    public void setCvv(String cvv) {
        creditCard.setCvv(cvv);
    }

}
