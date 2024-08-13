package Server;

public class DeliveryUser extends User{
    private double rating;
    private int numRatings;
    private double totalRating;

    public DeliveryUser(String userName, String hashedPassword, String address, String phoneNumber, String email, double rating, int numRatings, double totalRating) {
        super(userName, hashedPassword, address, phoneNumber, email);
        this.rating = rating;
        this.numRatings = numRatings;
        this.totalRating = totalRating;
    }

    public DeliveryUser(String csvLine) {
        super(csvLine);
        String[] fields = csvLine.split(",");
        try {
            this.rating = Double.parseDouble(fields[6]);
        } catch (NumberFormatException e) {
            this.rating = 0.0;
        }
        try {
            this.numRatings = Integer.parseInt(fields[7]);
        } catch (NumberFormatException e) {
            this.numRatings = 0;
        }
        try {
            this.totalRating = Double.parseDouble(fields[8]);
        } catch (NumberFormatException e) {
            this.totalRating = 0.0;
        }
    }

    @Override
    public void performUserSpecificAction() {

    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(double totalRating) {
        this.totalRating = totalRating;
    }
}
