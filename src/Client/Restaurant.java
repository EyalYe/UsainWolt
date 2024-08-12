package Client;

public class Restaurant {
    private String restaurantName;
    private double distance;
    private String profilePictureUrl;
    private String address;
    private String phoneNumber;
    private String cuisine;

    // Getters and Setters
    public String getName() { return restaurantName; }
    public void setName(String name) { this.restaurantName = name; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    @Override
    public String toString() {
        return restaurantName + " - " + distance + " km" + " - " + profilePictureUrl + " - " + address + " - " + phoneNumber + " - " + cuisine;
    }
}
