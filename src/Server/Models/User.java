package Server.Models;

import Server.Utilities.GeoLocationService;

public abstract class User {
    private String userName;
    private String hashedPassword;
    private String address;
    private String phoneNumber;
    private String email;
    private double[] location = new double[2]; // [latitude, longitude]

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

    public void setAddress(String address) {
        this.address = address;
        GeoLocationService geoLocationService = new GeoLocationService();
        try {
            this.location = geoLocationService.getCoordinates(address);
        } catch (Exception e) {
            System.out.println("Error getting coordinates for address: " + address);
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

    public boolean checkPassword(String password) {
        return hashedPassword.equals(password);
    }


    public String toString() {
        return userName + "," + hashedPassword + "," + address + "," + phoneNumber + "," + email + "," + location[0] + "," + location[1];
    }

    // Abstract method to be implemented by subclasses
    public abstract void performUserSpecificAction();

    // Main method for testing purposes
    public static void main(String[] args) {
        // Testing is not possible on abstract class. Create instances of subclasses to test.
    }

    public double[] getCoordinates() {
        return location;
    }
}
