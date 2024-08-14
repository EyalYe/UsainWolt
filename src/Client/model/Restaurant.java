package Client.model;

public class Restaurant {
    private String restaurantName;
    private double distance;
    private String profilePictureUrl;
    private String address;
    private String phoneNumber;
    private String cuisine;
    private String restaurantActualName;

    // Getters and Setters
    public String getName() { return restaurantName; }
    public void setName(String name) { this.restaurantName = name; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public String getRestaurantActualName() { return restaurantActualName; }
    public void setRestaurantActualName(String restaurantActualName) { this.restaurantActualName = restaurantActualName; }


    @Override
    public String toString() {
        return restaurantName + " - " + distance + " km" + " - " + profilePictureUrl + " - " + address + " - " + phoneNumber + " - " + cuisine;
    }
}
