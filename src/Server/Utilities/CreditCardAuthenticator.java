// Group: 6
package Server.Utilities;

/**
 * CreditCardAuthenticator is a utility class that authenticates credit card information.
 * It checks if the credit card number, expiration date, and CVV are valid.
 */

public class CreditCardAuthenticator {

    public boolean authenticate(String creditCardNumber, String expirationDate, String cvv) {
        // Mock authentication logic
        // In real life, this would be a call to a payment gateway
        if (creditCardNumber != null && creditCardNumber.length() == 16 && expirationDate != null && expirationDate.length() == 5 && cvv != null && cvv.length() == 3){
            return true;
        }
        return false;
    }

    public boolean makePayment(String creditCardNumber, String expirationDate, String cvv, double amount) {
        // Mock payment logic
        // In real life, this would be a call to a payment gateway
        if (authenticate(creditCardNumber, expirationDate, cvv)) {
            System.out.println("Payment of $" + amount + " made successfully.");
            return true;
        }
        System.out.println("Payment failed.");
        return false;
    }
}
