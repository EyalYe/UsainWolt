// Group: 6
package Server.Utilities;
import Server.Utilities.CustomDateAdapter.*;

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

/**
 * GeoLocationService provides utilities for working with geographic locations.
 * This class includes methods for:
 *
 * 1. Retrieving an API key from a configuration file.
 * 2. Validating an address using the OpenCage geocoding API.
 * 3. Getting the latitude and longitude of an address using the OpenCage geocoding API.
 * 4. Calculating the distance between two geographic coordinates using the Haversine formula.
 * 5. Calculating the distance between two addresses by obtaining their coordinates first.
 * 6. Checking if the distance between two addresses is within a specified range.
 *
 * The service relies on the OpenCage geocoding API and requires an API key, which should be
 * stored in a file named ".env" in the "src/Server" directory. It uses the Gson library
 * for JSON parsing and handles HTTP requests using HttpURLConnection.
 */
public class GeoLocationService {
    // Retrieve the API key from a properties file
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

    private static final String OPEN_CAGE_API_KEY = getApiKey(); // API key for OpenCage geocoding service
    private static final String OPEN_CAGE_API_URL = "https://api.opencagedata.com/geocode/v1/json?q=%s&key=%s"; // URL template for the API
    private final Gson gson = CustomDateAdapter.gsonCreator(); // Gson instance for JSON parsing

    // Validate the address using OpenCage API
    public boolean validateAddress(String address) throws IOException {
        String url = String.format(OPEN_CAGE_API_URL, address.replace(" ", "%20"), OPEN_CAGE_API_KEY);
        String response = sendGetRequest(url); // Send GET request to the API
        Map<String, Object> result = gson.fromJson(response, Map.class); // Parse response JSON into a map

        if (result != null && result.containsKey("results")) {
            List<?> results = (List<?>) result.get("results");
            return results != null && !results.isEmpty(); // Check if results are present
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

        double dLat = Math.toRadians(lat2 - lat1); // Difference in latitude
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c; // Return distance in kilometers
    }

    // Calculate the distance between two addresses
    public double calculateDistance(String address1, String address2) {
        try {
            double[] coordinates1 = getCoordinates(address1);
            double[] coordinates2 = getCoordinates(address2);
            if (coordinates1 != null && coordinates2 != null) {
                double lat1 = coordinates1[0];
                double lon1 = coordinates1[1];
                double lat2 = coordinates2[0];
                double lon2 = coordinates2[1];
                return calculateDistance(lat1, lon1, lat2, lon2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if distance cannot be calculated
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

    // Check if the distance between two addresses is less than or equal to a specified distance
    public static boolean checkSmallDistance(String address1, String address2, double distance) {
        GeoLocationService geoLocationService = new GeoLocationService();
        try {
            double[] coordinates1 = geoLocationService.getCoordinates(address1);
            double[] coordinates2 = geoLocationService.getCoordinates(address2);
            if (coordinates1 != null && coordinates2 != null) {
                double lat1 = coordinates1[0];
                double lon1 = coordinates1[1];
                double lat2 = coordinates2[0];
                double lon2 = coordinates2[1];
                double calculatedDistance = geoLocationService.calculateDistance(lat1, lon1, lat2, lon2);
                // Check if calculated distance is within the specified distance
                return calculatedDistance <= distance;
            }
        } catch (IOException e) {
            e.printStackTrace(); // Print error if unable to get coordinates
        }
        return false; // Return false if distance check fails
    }
}
