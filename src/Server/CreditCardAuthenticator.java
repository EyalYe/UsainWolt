package Server;

public class CreditCardAuthenticator {

    public boolean authenticate(String creditCardNumber, String expirationDate, String cvv) {
        // Mock authentication logic
        // For simplicity, let's assume that any credit card number that ends with "0" is valid
        // This is purely for testing purposes and should be replaced with actual logic in a real system
        if (creditCardNumber != null && creditCardNumber.endsWith("0")) {
            return true;
        }
        return false;
    }
}
