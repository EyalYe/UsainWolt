package Server;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GeoLocationService {
    public static String getApiKey() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/Server/.env")) {
            properties.load(input);
            return properties.getProperty("APIKEY");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String OPEN_CAGE_API_KEY = getApiKey();
    private static final String OPEN_CAGE_API_URL = "https://api.opencagedata.com/geocode/v1/json?q=%s&key=%s";
    private final Gson gson = new Gson();

    // Validate the address using OpenCage API
    public boolean validateAddress(String address) throws IOException {
        String url = String.format(OPEN_CAGE_API_URL, address.replace(" ", "%20"), OPEN_CAGE_API_KEY);
        String response = sendGetRequest(url);
        Map<String, Object> result = gson.fromJson(response, Map.class);

        if (result != null && result.containsKey("results")) {
            List<?> results = (List<?>) result.get("results");
            return results != null && !results.isEmpty();
        }
        return false;
    }

    // Get latitude and longitude from the address
    public double[] getCoordinates(String address) throws IOException {
        String url = String.format(OPEN_CAGE_API_URL, address.replace(" ", "%20"), OPEN_CAGE_API_KEY);
        String response = sendGetRequest(url);
        Map<String, Object> result = gson.fromJson(response, Map.class);

        if (result != null && result.containsKey("results")) {
            List<?> results = (List<?>) result.get("results");
            if (results != null && !results.isEmpty()) {
                Map<String, Object> geometry = (Map<String, Object>) ((Map<String, Object>) results.get(0)).get("geometry");
                double latitude = (double) geometry.get("lat");
                double longitude = (double) geometry.get("lng");
                return new double[]{latitude, longitude};
            }
        }
        return null;
    }

    // Calculate the distance between two points using the Haversine formula
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius of Earth in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    // Send a GET request using HttpURLConnection
    private String sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } else {
            throw new IOException("GET request failed with response code: " + responseCode);
        }
    }

    public static void main(String[] args) {
        GeoLocationService geoLocationService = new GeoLocationService();

        // Test address validation and retrieval of coordinates
        String testAddress = "1600 Amphitheatre Parkway, Mountain View, CA";

        try {
            // Validate the address
            boolean isValid = geoLocationService.validateAddress(testAddress);
            System.out.println("Is the address valid? " + isValid);

            if (isValid) {
                // Get coordinates of the address
                double[] coordinates = geoLocationService.getCoordinates(testAddress);
                if (coordinates != null) {
                    double latitude = coordinates[0];
                    double longitude = coordinates[1];
                    System.out.println("Latitude: " + latitude);
                    System.out.println("Longitude: " + longitude);

                    // Calculate the distance between this address and another location
                    double anotherLatitude = 37.7749; // Latitude of San Francisco, CA
                    double anotherLongitude = -122.4194; // Longitude of San Francisco, CA

                    double distance = geoLocationService.calculateDistance(latitude, longitude, anotherLatitude, anotherLongitude);
                    System.out.println("Distance to San Francisco: " + distance + " km");
                } else {
                    System.out.println("Could not retrieve coordinates.");
                }
            } else {
                System.out.println("Invalid address.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
