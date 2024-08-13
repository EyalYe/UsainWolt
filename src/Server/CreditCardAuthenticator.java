package Server;

public class CreditCardAuthenticator {

    public boolean authenticate(String creditCardNumber, String expirationDate, String cvv) {
        // Mock authentication logic
        // In real life, this would be a call to a payment gateway
        if (creditCardNumber != null && creditCardNumber.length() == 16 && expirationDate != null && expirationDate.length() == 5 && cvv != null && cvv.length() == 3){
            return true;
        }
        return false;
    }
}
