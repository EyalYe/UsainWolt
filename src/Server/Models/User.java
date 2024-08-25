package Server.Models;

import Server.Utilities.GeoLocationService;

public abstract class User {
    private String userName; // Username of the user
    private String hashedPassword; // Hashed password for authentication
    private String address; // Address of the user
    private String phoneNumber; // Phone number of the user
    private String email; // Email address of the user
    private double[] location = new double[2]; // Geographic location [latitude, longitude]

    // Constructor to initialize a User object with details and obtain coordinates from address
    public User(String userName, String hashedPassword, String address, String phoneNumber, String email) {
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        GeoLocationService geoLocationService = new GeoLocationService();
        try {
            this.location = geoLocationService.getCoordinates(address);
        } catch (Exception e) {
            System.out.println("Error getting coordinates for address: " + address);
        }
    }

    // Constructor to initialize a User object from a CSV line
    public User(String csvLine) {
        String[] fields = csvLine.split(",");
        this.userName = fields[1];
        this.hashedPassword = fields[2];
        this.address = fields[3];
        this.phoneNumber = fields[4];
        this.email = fields[5];
        this.location[0] = Double.parseDouble(fields[6]);
        this.location[1] = Double.parseDouble(fields[7]);
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

    public void setAddress(String address) throws RuntimeException {
        this.address = address;
        GeoLocationService geoLocationService = new GeoLocationService();
        try {
            // Update coordinates when address changes
            this.location = geoLocationService.getCoordinates(address);
        } catch (Exception e) {
            // Handle errors in updating coordinates
            throw new RuntimeException("Error getting coordinates for address: " + address);
        }
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

    // Method to check if the provided password matches the hashed password
    public boolean checkPassword(String password) {
        return hashedPassword.equals(password);
    }


    public String toString() {
        // Return a string representation of the User object
        return userName + "," + hashedPassword + "," + address + "," + phoneNumber + "," + email + "," + location[0] + "," + location[1];
    }

    // Abstract method to be implemented by subclasses
    public abstract void performUserSpecificAction();

    // Main method for testing purposes
    public static void main(String[] args) {
        // Testing is not possible on abstract class. Create instances of subclasses to test.
    }

    // Method to get the geographic coordinates of the user
    public double[] getCoordinates() {
        return location;
    }
}
