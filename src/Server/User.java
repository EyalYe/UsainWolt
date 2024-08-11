package Server;

public abstract class User {
    private String userName;
    private String hashedPassword;
    private String address;
    private String phoneNumber;
    private String email;

    public User(String userName, String hashedPassword, String address, String phoneNumber, String email) {
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // Getters and Setters

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Abstract method to be implemented by subclasses
    public abstract void performUserSpecificAction();

    // Main method for testing purposes
    public static void main(String[] args) {
        // Testing is not possible on abstract class. Create instances of subclasses to test.
    }
}
