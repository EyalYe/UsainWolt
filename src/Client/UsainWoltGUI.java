package Client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class UsainWoltGUI {
    private JFrame frame;
    private ClientApp clientApp;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Timer responsePoller;
    private String[] availableCuisines;
    private Gson gson = new Gson();

    public UsainWoltGUI(ClientApp clientApp) {
        this.clientApp = clientApp;
        this.availableCuisines = new String[0];
        initialize();
        startResponsePolling();
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getAvailableCuisines");
        clientApp.addRequest(request);
    }

    private JLabel connectionStatusLabel;

    private void initialize() {
        // Setup the main frame
        frame = new JFrame("Usain Wolt");
        frame.setSize(1050, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Add connection status label
        connectionStatusLabel = new JLabel("Not connected");
        frame.add(connectionStatusLabel, BorderLayout.NORTH);

        // Call the method to generate the login screen
        generateLogin();

        // Set the frame visible at the end
        frame.setVisible(true);
    }

    private void updateConnectionStatus(String status) {
        SwingUtilities.invokeLater(() -> connectionStatusLabel.setText(status));
    }


    private void startResponsePolling() {
        responsePoller = new Timer(100, e -> {
            Map<String, Object> response = clientApp.getResponse();
            if (response != null) {
                processResponse(response);  // Process the response
            }
        });
        responsePoller.start();  // Start the timer to poll responses
    }

    private void processResponse(Map<String, Object> response) {
        String requestType = (String) response.get("type");
        closeLoading();
        if("false".equals(response.get("success"))) {
            JOptionPane.showMessageDialog(frame, "Error: " + response.get("message"));
            return;
        }

        switch (requestType) {
            case "handleLogin":
                handleLoginResponse(response);
                break;
            case "handleSignupCustomer":
                handleSignupResponse(response);
                break;
            case "handleSignupRestaurant":
                handleSignupResponse(response);
                break;
            case "handleSignupDelivery":
                handleSignupResponse(response);
                break;
            case "handleGetAvailableCuisines":
                availableCuisines = ((String) response.get("message")).split(",");
                break;
            case "handleGetRestaurants":
                Type restaurantListType = new TypeToken<java.util.List<Restaurant>>(){}.getType();
                List<Restaurant> restaurants = gson.fromJson((String) response.get("message"), restaurantListType);
                showRestaurants(restaurants);
                break;
            case "handleGetMenu":
                Type menuListType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                showMenu(gson.fromJson((String) response.get("message"), menuListType));
                break;
            case "handlePlaceOrder":
                if ("true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Order placed successfully!");
                    showOrderHistory();
                } else {
                    JOptionPane.showMessageDialog(frame, "Error placing order: " + response.get("message"));
                }
        }


    }

    private void handleLoginResponse(Map<String, Object> response) {
        if ("true".equals(response.get("success"))) {
            JOptionPane.showMessageDialog(frame, "Login successful!");
            switch ((String) response.get("message")) {
                case "Logged in as customer":
                    generateCustomerUI();
                    break;
                case "Logged in as restaurant":
                    generateRestaurantUI();
                    break;
                case "Logged in as delivery":
                    generateDeliveryUI();
                    break;
                case "Logged in as admin":
                    generateAdminUI();
                    break;
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Login failed: " + response.get("message"));
        }
    }

    private void handleSignupResponse(Map<String, Object> response) {
        if ("true".equals(response.get("success"))) {
            JOptionPane.showMessageDialog(frame, "Signup successful!");
            generateLogin();  // Redirect back to login after successful signup
        } else {
            JOptionPane.showMessageDialog(frame, "Signup failed: " + response.get("message"));
        }
    }

    private void handleLogout() {
        Map<String,Object> request = new HashMap<>();
        request.put("type", "disconnect");
        request.put("username", usernameField.getText());
        request.put("password", new String(passwordField.getPassword()));
        clientApp.addRequest(request);
        generateLogin();
    }

    private void generateLogin() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Add image to the left side
        String imagePath = "src/Client/LOGO.jpg";  // Update this to the correct path
        ImageIcon icon = new ImageIcon(imagePath);

        // Resize the image to fit the JLabel if necessary
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(600, 600, Image.SCALE_SMOOTH);
        icon = new ImageIcon(resizedImg);

        JLabel imageLabel = new JLabel(icon);
        frame.add(imageLabel, BorderLayout.WEST);

        // Create login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create labels and fields for username and password
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);  // Use instance variable here

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);  // Use instance variable here

        // Add Username label and text field
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        loginPanel.add(usernameField, gbc);

        // Add Password label and password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(passwordField, gbc);

        // Create and add Login and Signup buttons
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Sign Up");

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        gbc.gridy = 3;
        loginPanel.add(signupButton, gbc);

        // Add action listener for login button
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try {
                Map<String, Object> request = new HashMap<>();
                request.put("type", "login");
                request.put("username", username);
                request.put("password", password);

                clientApp.addRequest(request);  // Enqueue the request
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });

        // Add action listener for signup button
        signupButton.addActionListener(e -> generateSignup());

        frame.add(loginPanel, BorderLayout.EAST); // Place the login panel on the right side
        frame.revalidate();
        frame.repaint();
    }

    private void generateSignup() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Add image to the left side (keeping the image)
        String imagePath = "src/Client/LOGO.jpg";  // Update this to the correct path
        ImageIcon icon = new ImageIcon(imagePath);

        // Resize the image to fit the JLabel if necessary
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(600, 600, Image.SCALE_SMOOTH);
        icon = new ImageIcon(resizedImg);

        JLabel imageLabel = new JLabel(icon);
        frame.add(imageLabel, BorderLayout.WEST);

        // Create a panel for the right side with GridBagLayout
        JPanel signupPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Ensure the form is centered
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Add Back to Login button in the top left
        JButton backButton = new JButton("Back to Login");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        signupPanel.add(backButton, gbc);

        // Add UserType dropdown in the top middle
        JComboBox<String> userTypeDropdown = new JComboBox<>(new String[]{"Customer", "Restaurant", "Delivery"});
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        signupPanel.add(userTypeDropdown, gbc);

        // Create a panel for the signup form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        signupPanel.add(formPanel, gbc);

        // Method to add a field to the formPanel
        JTextField signupUsernameField = addFormField(formPanel, "Username", 0);
        JPasswordField signupPasswordField = (JPasswordField) addFormField(formPanel, "Password", 1, true);
        JPasswordField confirmPasswordField = (JPasswordField) addFormField(formPanel, "Confirm Password", 2, true);
        JTextField emailField = addFormField(formPanel, "Email", 3);
        JTextField addressField = addFormField(formPanel, "Address", 4);
        JTextField phoneNumberField = addFormField(formPanel, "Phone Number", 5);

        // Placeholder fields for additional fields
        JTextField businessPhoneField = new JTextField(20);
        String cuisine_options = "";
        try {
            cuisine_options = ((String) clientApp.getAvailableCuisines().get("message")).replace(",All", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JComboBox<String> cuisineDropdown = new JComboBox<>(cuisine_options.split(","));
        JTextField tokenField = new JTextField(20);

        // Submit button
        JButton submitButton = new JButton("Submit");
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        formPanel.add(submitButton, gbc);

        // Action to update the form based on the selected user type
        userTypeDropdown.addActionListener(e -> {
            formPanel.removeAll(); // Clear the formPanel

            // Add common fields
            addFormField(formPanel, "Username", 0, signupUsernameField);
            addFormField(formPanel, "Password", 1, signupPasswordField, true);
            addFormField(formPanel, "Confirm Password", 2, confirmPasswordField, true);
            addFormField(formPanel, "Email", 3, emailField);
            addFormField(formPanel, "Address", 4, addressField);
            addFormField(formPanel, "Phone Number", 5, phoneNumberField);

            String userType = (String) userTypeDropdown.getSelectedItem();
            if ("Restaurant".equals(userType)) {
                addFormField(formPanel, "Business Phone", 6, businessPhoneField);
                addComboBoxField(formPanel, "Cuisine", 7, cuisineDropdown);
                gbc.gridy = 8;
            } else if ("Delivery".equals(userType)) {
                addFormField(formPanel, "Acceptance Token", 6, tokenField);
                gbc.gridy = 7;
            } else {
                gbc.gridy = 6;
            }

            // Add submit button at the bottom
            formPanel.add(submitButton, gbc);

            formPanel.revalidate();
            formPanel.repaint();
        });

        backButton.addActionListener(e -> generateLogin()); // Go back to the login screen

        submitButton.addActionListener(e -> {
            String userType = (String) userTypeDropdown.getSelectedItem();
            String username = signupUsernameField.getText();
            String password = new String(signupPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText();
            String address = addressField.getText();
            String phoneNumber = phoneNumberField.getText();

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(frame, "Passwords do not match. Please try again.");
                return;
            }

            try {
                Map<String, Object> request = new HashMap<>();
                request.put("username", username);
                request.put("password", password);
                request.put("address", address);
                request.put("phoneNumber", phoneNumber);
                request.put("email", email);

                if ("Customer".equals(userType)) {
                    request.put("type", "signupCustomer");
                } else if ("Restaurant".equals(userType)) {
                    String businessPhoneNumber = businessPhoneField.getText();
                    String cuisine = (String) cuisineDropdown.getSelectedItem();
                    request.put("type", "signupRestaurant");
                    request.put("businessPhoneNumber", businessPhoneNumber);
                    request.put("cuisine", cuisine);
                } else if ("Delivery".equals(userType)) {
                    String token = tokenField.getText();
                    request.put("type", "signupDelivery");
                    request.put("token", token);
                }

                clientApp.addRequest(request);  // Enqueue the request

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error during signup: " + ex.getMessage());
            }
        });

        // Add signupPanel to the right side
        frame.add(signupPanel, BorderLayout.EAST);
        frame.revalidate();
        frame.repaint();
    }

    // Method to add form fields for text fields and password fields
    private JTextField addFormField(JPanel panel, String labelText, int yPos) {
        return addFormField(panel, labelText, yPos, new JTextField(20), false);
    }

    private JPasswordField addFormField(JPanel panel, String labelText, int yPos, boolean isPasswordField) {
        return (JPasswordField) addFormField(panel, labelText, yPos, new JPasswordField(20), true);
    }

    private JTextField addFormField(JPanel panel, String labelText, int yPos, JTextField textField) {
        return addFormField(panel, labelText, yPos, textField, false);
    }

    private JTextField addFormField(JPanel panel, String labelText, int yPos, JTextField textField, boolean isPasswordField) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = yPos;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = yPos;
        panel.add(textField, gbc);

        return textField;
    }

    // Method to add form fields for JComboBox
    private void addComboBoxField(JPanel panel, String labelText, int yPos, JComboBox<String> comboBox) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = yPos;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = yPos;
        panel.add(comboBox, gbc);
    }

    // -------------------------------------- UI for Customer --------------------------------------

    private void generateCustomerUI() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Create a panel for the sidebar with buttons
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(4, 1, 10, 10));
        sidebar.setPreferredSize(new Dimension(200, frame.getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the sidebar

        // Create the buttons
        JButton placeOrderButton = new JButton("Place Order");
        JButton viewOrderHistoryButton = new JButton("View Order History");
        JButton userSettingsButton = new JButton("User Settings");
        JButton logoutButton = new JButton("Logout");

        // Add action listeners for buttons
        placeOrderButton.addActionListener(e -> showPlaceOrder());
        viewOrderHistoryButton.addActionListener(e -> showOrderHistory());
        userSettingsButton.addActionListener(e -> showUserSettings());
        logoutButton.addActionListener(e -> handleLogout());

        // Add buttons to the sidebar panel
        sidebar.add(placeOrderButton);
        sidebar.add(viewOrderHistoryButton);
        sidebar.add(userSettingsButton);
        sidebar.add(logoutButton);

        // Create a main content panel where the content will change based on button clicks
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the main content

        // Add a welcome label to the main content panel
        JLabel welcomeLabel = new JLabel("Welcome to Usain Wolt!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainContentPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Add the sidebar and main content panels to the frame
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(mainContentPanel, BorderLayout.CENTER);

        // Refresh the frame to display the new UI
        frame.revalidate();
        frame.repaint();
    }

    // Method to show place order screen
    private void showPlaceOrder() {
        // Clear the main content panel first
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        // Create a panel for the search criteria (top part)
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between components

        // Add distance slider
        JLabel distanceLabel = new JLabel("Distance:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(distanceLabel, gbc);

        JSlider distanceSlider = new JSlider(0, 30, 10); // 1 to 50 km, default 10 km
        distanceSlider.setMajorTickSpacing(10);
        distanceSlider.setMinorTickSpacing(1);
        distanceSlider.setPaintTicks(true);
        distanceSlider.setPaintLabels(true);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(distanceSlider, gbc);

        // Add cuisine dropdown
        JLabel cuisineLabel = new JLabel("Cuisine:");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(cuisineLabel, gbc);

        String[] cuisines = this.availableCuisines;
        JComboBox<String> cuisineDropdown = new JComboBox<>(cuisines);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(cuisineDropdown, gbc);

        // Add search button
        JButton searchButton = new JButton("Search");
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(searchButton, gbc);

        // Add action listener for search button
        searchButton.addActionListener(e -> {
            int distance = distanceSlider.getValue();
            String selectedCuisine = (String) cuisineDropdown.getSelectedItem();
            try {
                performSearch(distance, selectedCuisine);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        // Add the search panel to the top of the main content panel
        mainContentPanel.add(searchPanel, BorderLayout.NORTH);

        // Placeholder for search results panel (will be populated after search)
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh the main content panel to display the new UI
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void performSearch(int distance, String selectedCuisine) throws Exception {
       clientApp.searchRestaurantsAsync(usernameField.getText(), new String(passwordField.getPassword()), selectedCuisine, String.valueOf(distance));
       showLoading();
    }

    private void showRestaurants(List<Restaurant> restaurants) {
        // Get the main content panel to clear and update
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        showPlaceOrder();  // Show the search panel again

        // Create a panel to list the restaurants
        JPanel restaurantsPanel = new JPanel();
        restaurantsPanel.setLayout(new BoxLayout(restaurantsPanel, BoxLayout.Y_AXIS));  // Ensure vertical alignment

        if (restaurants.isEmpty()) {
            JLabel noResultsLabel = new JLabel("No restaurants found.");
            noResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            restaurantsPanel.add(noResultsLabel);
        } else {
            for (Restaurant restaurant : restaurants) {
                JPanel restaurantPanel = createRestaurantPanel(restaurant);
                restaurantsPanel.add(restaurantPanel);
                restaurantsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between restaurants
            }
        }

        // Add the restaurants panel to the main content panel
        JScrollPane scrollPane = new JScrollPane(restaurantsPanel);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh the main content panel to display the new UI
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private JPanel createRestaurantPanel(Restaurant restaurant) {
        JPanel restaurantPanel = new JPanel(new BorderLayout(10, 10));
        restaurantPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        restaurantPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Profile Picture on the Left
        JLabel profilePictureLabel = new JLabel(loadImageIcon(restaurant.getProfilePictureUrl(), 100, 100));
        restaurantPanel.add(profilePictureLabel, BorderLayout.WEST);

        // Text and Icons in the Center
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Adjust padding here
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Restaurant Name with Icon
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel nameIconLabel = new JLabel(loadImageIcon("icons/restaurant_name.png", 24, 24));
        infoPanel.add(nameIconLabel, gbc);

        gbc.gridx = 1;
        JLabel nameLabel = new JLabel(restaurant.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoPanel.add(nameLabel, gbc);

        // Row 1: Distance with Icon
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel distanceIconLabel = new JLabel(loadImageIcon("icons/distance.png", 24, 24));
        infoPanel.add(distanceIconLabel, gbc);

        gbc.gridx = 1;
        JLabel distanceLabel = new JLabel(String.format("%.1f km", restaurant.getDistance()));
        distanceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        infoPanel.add(distanceLabel, gbc);

        // Row 2: Cuisine with Icon
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel cuisineIconLabel = new JLabel(loadImageIcon("icons/cuisine.png", 24, 24));
        infoPanel.add(cuisineIconLabel, gbc);

        gbc.gridx = 1;
        JLabel cuisineLabel = new JLabel(restaurant.getCuisine());
        cuisineLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        infoPanel.add(cuisineLabel, gbc);

        // Add the infoPanel to the restaurantPanel center
        restaurantPanel.add(infoPanel, BorderLayout.CENTER);

        // Phone and Address on the Right
        JPanel contactPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.insets = new Insets(5, 10, 5, 10); // Adjust padding here
        gbcRight.anchor = GridBagConstraints.WEST;

        // Row 0: Phone Number with Icon
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        JLabel phoneIconLabel = new JLabel(loadImageIcon("icons/phone.png", 24, 24));
        contactPanel.add(phoneIconLabel, gbcRight);

        gbcRight.gridx = 1;
        JLabel phoneLabel = new JLabel(restaurant.getPhoneNumber());
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        contactPanel.add(phoneLabel, gbcRight);

        // Row 1: Address with Icon
        gbcRight.gridx = 0;
        gbcRight.gridy = 1;
        JLabel addressIconLabel = new JLabel(loadImageIcon("icons/address.png", 24, 24));
        contactPanel.add(addressIconLabel, gbcRight);

        gbcRight.gridx = 1;
        JLabel addressLabel = new JLabel(restaurant.getAddress());
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        contactPanel.add(addressLabel, gbcRight);

        // Add the contactPanel to the restaurantPanel right
        restaurantPanel.add(contactPanel, BorderLayout.EAST);

        // Add action listener to handle clicks on the restaurant panel
        restaurantPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleRestaurantClick(restaurant);
            }
        });

        return restaurantPanel;
    }



    // Helper method to load and resize images
    private ImageIcon loadImageIcon(String pathOrUrl, int width, int height) {
        try {
            ImageIcon icon;
            if (pathOrUrl.startsWith("http")) {
                icon = new ImageIcon(new URL(pathOrUrl));
            } else {
                icon = new ImageIcon(pathOrUrl);
            }
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return new ImageIcon();  // Return an empty icon in case of error
        }
    }

    private String chosenRestaurant;
    private void handleRestaurantClick(Restaurant restaurant) {
        try {
            showLoading();  // Show a loading screen while fetching the menu
            clientApp.getMenuAsync(restaurant.getName());
            chosenRestaurant = restaurant.getName();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMenu(List<Map<String, Object>> menu) {
        // Clear the main content panel to show the menu
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        showPlaceOrder(); // Show the search panel again

        // Create a panel to list the menu items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));  // Ensure vertical alignment

        for (Map<String, Object> menuItem : menu) {
            JPanel menuItemPanel = createMenuItemPanel(menuItem);
            menuPanel.add(menuItemPanel);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between items
        }

        // Add the menu panel to a scroll pane in the main content panel
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Add a "Proceed to Checkout" button at the bottom
        JButton checkoutButton = new JButton("Proceed to Checkout");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.setPreferredSize(new Dimension(200, 50));
        checkoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutButton.addActionListener(e -> proceedToCheckout(menu));

        JPanel checkoutPanel = new JPanel();
        checkoutPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        checkoutPanel.add(checkoutButton);

        mainContentPanel.add(checkoutPanel, BorderLayout.SOUTH);

        // Refresh the main content panel to display the new UI
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private JPanel createMenuItemPanel(Map<String, Object> menuItem) {
        menuItem.put("quantity", 0);  // Add a quantity field to the menu item (default 0

        JPanel menuItemPanel = new JPanel(new BorderLayout(10, 10));
        menuItemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Photo on the Left
        String photoUrl = (String) menuItem.get("photoUrl");
        JLabel photoLabel = new JLabel(loadImageIcon(photoUrl, 100, 100));
        menuItemPanel.add(photoLabel, BorderLayout.WEST);

        // Text and Icons in the Center
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Name with Icon
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameIconLabel = new JLabel(loadImageIcon("icons/name.png", 24, 24));
        infoPanel.add(nameIconLabel, gbc);

        gbc.gridx = 1;
        JLabel nameLabel = new JLabel((String) menuItem.get("name"));
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(nameLabel, gbc);

        // Row 1: Price with Icon
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel priceIconLabel = new JLabel(loadImageIcon("icons/price.png", 24, 24));
        infoPanel.add(priceIconLabel, gbc);

        gbc.gridx = 1;
        JLabel priceLabel = new JLabel( menuItem.get("price").toString());
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        infoPanel.add(priceLabel, gbc);

        // Row 2: Description with Icon
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel descriptionIconLabel = new JLabel(loadImageIcon("icons/description.png", 24, 24));
        infoPanel.add(descriptionIconLabel, gbc);

        gbc.gridx = 1;
        JLabel descriptionLabel = new JLabel((String) menuItem.get("description"));
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        infoPanel.add(descriptionLabel, gbc);

        // Add the infoPanel to the menuItemPanel center
        menuItemPanel.add(infoPanel, BorderLayout.CENTER);

        // Quantity Counter on the Right
        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton minusButton = new JButton("-");
        JLabel quantityLabel = new JLabel("0");
        JButton plusButton = new JButton("+");

        minusButton.setPreferredSize(new Dimension(50, 30));
        plusButton.setPreferredSize(new Dimension(50, 30));

        // Action listeners to update the quantity
        minusButton.addActionListener(e -> {
            int quantity = Integer.parseInt(quantityLabel.getText());
            if (quantity > 0) {
                quantity--;
                menuItem.put("quantity", quantity);  // Update the quantity in the menu item (map
                quantityLabel.setText(String.valueOf(quantity));
            }
        });

        plusButton.addActionListener(e -> {
            int quantity = Integer.parseInt(quantityLabel.getText());
            quantity++;
            menuItem.put("quantity", quantity);  // Update the quantity in the menu item (map)
            quantityLabel.setText(String.valueOf(quantity));
        });

        counterPanel.add(minusButton);
        counterPanel.add(quantityLabel);
        counterPanel.add(plusButton);

        menuItemPanel.add(counterPanel, BorderLayout.EAST);

        return menuItemPanel;
    }

    private void proceedToCheckout(List<Map<String, Object>> menu) {
        // Create a new JFrame for the checkout window
        JFrame checkoutFrame = new JFrame("Checkout");
        checkoutFrame.setSize(700, 500);
        checkoutFrame.setLayout(new BorderLayout(5, 5));  // Reduced outer spacing
        checkoutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a panel to list the ordered items
        JPanel orderPanel = new JPanel();
        orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));  // Ensure vertical alignment

        final double[] totalAmount = {0.0}; // Use an array to hold the total amount

        for (Map<String, Object> menuItem : menu) {
            int quantity = Integer.parseInt(menuItem.get("quantity").toString());
            if (quantity > 0) {
                JPanel itemPanel = new JPanel(new BorderLayout(2, 2));  // Further reduced spacing between elements
                itemPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));  // Reduced padding around items
                JLabel itemName = new JLabel(menuItem.get("name") + " x" + quantity);
                double price = (double) menuItem.get("price") * quantity;
                totalAmount[0] += price;  // Access array to modify the value
                JLabel itemPrice = new JLabel(String.format("$%.2f", price));
                itemPanel.add(itemName, BorderLayout.WEST);
                itemPanel.add(itemPrice, BorderLayout.EAST);
                orderPanel.add(itemPanel);
            }
        }

        // Display the total amount
        JLabel totalLabel = new JLabel("Total: " + String.format("$%.2f", totalAmount[0]));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        orderPanel.add(totalLabel);

        // Create a panel for the right side with the address and payment fields
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);  // Tighten spacing around components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Add "Send Home" checkbox and address field
        JCheckBox sendHomeCheckbox = new JCheckBox("Send Home");
        rightPanel.add(sendHomeCheckbox, gbc);

        gbc.gridy++;
        JTextField addressField = new JTextField();
        addressField.setEnabled(!sendHomeCheckbox.isSelected());  // Initially disabled if "Send Home" is checked
        sendHomeCheckbox.addActionListener(e -> addressField.setEnabled(!sendHomeCheckbox.isSelected()));
        rightPanel.add(new JLabel("Address:"), gbc);
        gbc.gridy++;
        rightPanel.add(addressField, gbc);

        // Add "Use Saved Card" checkbox and payment fields
        gbc.gridy++;
        JCheckBox useSavedCardCheckbox = new JCheckBox("Use Saved Card");
        rightPanel.add(useSavedCardCheckbox, gbc);

        gbc.gridy++;
        JTextField cardNumberField = new JTextField();
        rightPanel.add(new JLabel("Card Number:"), gbc);
        gbc.gridy++;
        rightPanel.add(cardNumberField, gbc);

        gbc.gridy++;
        JTextField expirationDateField = new JTextField();
        rightPanel.add(new JLabel("Expiration Date:"), gbc);
        gbc.gridy++;
        rightPanel.add(expirationDateField, gbc);

        gbc.gridy++;
        JTextField cvvField = new JTextField();
        rightPanel.add(new JLabel("CVV:"), gbc);
        gbc.gridy++;
        rightPanel.add(cvvField, gbc);

        useSavedCardCheckbox.addActionListener(e -> {
            boolean useSaved = useSavedCardCheckbox.isSelected();
            cardNumberField.setEnabled(!useSaved);
            expirationDateField.setEnabled(!useSaved);
            cvvField.setEnabled(!useSaved);
        });

        // Add a field for additional notes
        gbc.gridy++;
        rightPanel.add(new JLabel("Note:"), gbc);
        gbc.gridy++;
        JTextField noteField = new JTextField();
        rightPanel.add(noteField, gbc);

        // Add Place Order button
        gbc.gridy++;
        JButton placeOrderButton = new JButton("Place Order");

        placeOrderButton.addActionListener(e -> {
            String address = addressField.getText();
            String cardNumber = cardNumberField.getText();
            String expirationDate = expirationDateField.getText();
            String cvv = cvvField.getText();
            boolean sendHome = sendHomeCheckbox.isSelected();
            boolean useSavedCard = useSavedCardCheckbox.isSelected();
            String note = noteField.getText();

            placeOrder(totalAmount[0], address, cardNumber, expirationDate, cvv, sendHome, useSavedCard, note, menu);
            // Close the checkout window after placing the order
            checkoutFrame.dispose();
        });



        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));  // Adjust space before button
        rightPanel.add(placeOrderButton, gbc);

        // Add components to the checkout frame
        checkoutFrame.add(new JScrollPane(orderPanel), BorderLayout.WEST);
        checkoutFrame.add(rightPanel, BorderLayout.EAST);

        // Show the checkout frame
        checkoutFrame.setVisible(true);
    }


    // Method to place the order
    private void placeOrder(double totalAmount, String address, String cardNumber, String expirationDate, String cvv, boolean sendHome, boolean useSavedCard, String note, List<Map<String, Object>> menu) {
        try{
            Map<String, Object> request = new HashMap<>();
            request.put("type", "placeOrder");
            request.put("username", usernameField.getText());
            request.put("password", new String(passwordField.getPassword()));
            request.put("restaurantName", chosenRestaurant);
            request.put("items", menu);
            request.put("customerNote", note);
            request.put("useSavedCard", String.valueOf(useSavedCard));
            request.put("creditCardNumber", cardNumber);
            request.put("expirationDate", expirationDate);
            request.put("cvv", cvv);
            request.put("sendHome", String.valueOf(sendHome));
            request.put("address", address);
            request.put("totalAmount", totalAmount);
            clientApp.addRequest(request);
            showLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to show order history screen
    private void showOrderHistory() {
        JOptionPane.showMessageDialog(frame, "Feature to view order history coming soon!");
    }

    // Method to show user settings screen
    private void showUserSettings() {
        JOptionPane.showMessageDialog(frame, "Feature to manage user settings coming soon!");
    }


    // -------------------------------------- UI for Restaurant --------------------------------------
    private void generateRestaurantUI() {
        // Implement the UI for the restaurant here
    }
    // -------------------------------------- UI for Delivery --------------------------------------
    private void generateDeliveryUI() {
        // Implement the UI for the delivery here
    }

    // -------------------------------------- UI for Admin --------------------------------------
    private void generateAdminUI() {
        // Implement the UI for the admin here
    }

    // -------------------------------------Loading screen--------------------------------------
    private JDialog loadingDialog;

    // Method to show the loading dialog
    private void showLoading() {
        if (loadingDialog == null) {
            // Create a new JDialog with a loading message
            loadingDialog = new JDialog(frame, "Loading", true);
            loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            loadingDialog.setSize(200, 100);
            loadingDialog.setLayout(new BorderLayout());

            JLabel loadingLabel = new JLabel("Loading, please wait...", SwingConstants.CENTER);
            loadingDialog.add(loadingLabel, BorderLayout.CENTER);

            // Optionally, you can add a loading icon or animation
            // loadingLabel.setIcon(new ImageIcon("path/to/loading.gif"));

            // Center the dialog on the parent frame
            loadingDialog.setLocationRelativeTo(frame);
        }

        // Show the loading dialog
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
    }

    // Method to close the loading dialog
    private void closeLoading() {
        if (loadingDialog != null) {
            // Hide and dispose of the loading dialog
            SwingUtilities.invokeLater(() -> {
                loadingDialog.setVisible(false);
                loadingDialog.dispose();
                loadingDialog = null; // Reset the reference for future use
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClientApp clientApp = new ClientApp("localhost", 12345);
                Thread clientThread = new Thread(clientApp);
                clientThread.start();

                new UsainWoltGUI(clientApp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
