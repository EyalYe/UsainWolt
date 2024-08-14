import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import Server.Models.CustomerUser;
import Server.Models.RestaurantUser;


public class Main {
    public static void main(String[] args) {
        // fixing the users to fit the new structure
        List<String> users = new ArrayList<>();
        File usersFile = new File("users.csv");
        if (usersFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(usersFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length > 0) {
                        String userType = fields[0];
                        switch (userType) {
                            case "Customer":
                                CustomerUser customer = new CustomerUser(fields[1], fields[2], fields[3], fields[4], fields[5]);
                                users.add(customer.toString());
                                break;
                            case "Restaurant":
                                RestaurantUser restaurant = new RestaurantUser(fields[1], fields[2], fields[3], fields[4], fields[5], fields[8], fields[9], Double.parseDouble(fields[10]));
                                users.add(restaurant.toString());
                                break;
                            default:
                                System.out.println("Invalid user type: " + userType);
                        }
                    }
                }

                reader.close();

                // Write the updated users back to the file
                try (FileOutputStream fos = new FileOutputStream(usersFile)) {
                    for (String user : users) {
                        fos.write((user + "\n").getBytes());
                    }
                } catch (Exception e) {
                    System.out.println("Error writing users file: " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("Error reading users file: " + e.getMessage());
            }
        } else {
            System.out.println("Users file not found.");
        }
    }
}