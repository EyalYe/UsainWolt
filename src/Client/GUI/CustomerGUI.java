package Client.GUI;

import Client.model.Restaurant;
import Client.network.ClientApp;
import Server.Models.Order;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomerGUI {
    private final JFrame frame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final ClientApp clientApp;
    private String[] availableCuisines;
    private final LogoutCallback logoutCallback;

    CustomerGUI(JFrame frame, JTextField usernameField, JPasswordField passwordField, ClientApp clientApp, String[] availableCuisines , LogoutCallback logoutCallback) {
        this.frame = frame;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.clientApp = clientApp;
        this.availableCuisines = availableCuisines;
        this.logoutCallback = logoutCallback;
    }

    void generateCustomerUI() {
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

    private void handleLogout() {
        logoutCallback.onLogout();
    }

    // Method to show place order screen
    private void showPlaceOrder() {
        if (availableCuisines == null || availableCuisines.length == 0) {
            Map<String, Object> request = new HashMap<>();
            request.put("type", "getCuisines");
            clientApp.addRequest(request);
            this.availableCuisines = logoutCallback.getCuisines();
        }
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

        // Add Send Home checkbox
        JCheckBox sendHomeCheckbox = new JCheckBox("Send Home");
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(sendHomeCheckbox, gbc);

        // Add Address label
        JLabel addressLabel = new JLabel("Address:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(addressLabel, gbc);


        // Add Address field
        JTextField addressField = new JTextField();
        addressField.setEnabled(!sendHomeCheckbox.isSelected());
        sendHomeCheckbox.addActionListener(e -> {
                addressField.setEnabled(!sendHomeCheckbox.isSelected());
                addressLabel.setEnabled(!sendHomeCheckbox.isSelected());
        });
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(addressField, gbc);


        // Add action listener for search button
        searchButton.addActionListener(e -> {
            int distance = distanceSlider.getValue();
            String selectedCuisine = (String) cuisineDropdown.getSelectedItem();
            boolean sendHome = sendHomeCheckbox.isSelected();
            String address = addressField.getText();

            try {
                performSearch(distance, selectedCuisine, sendHome, address);
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

    private void performSearch(int distance, String selectedCuisine, boolean sendHome, String address) {
        clientApp.searchRestaurantsAsync(usernameField.getText(), new String(passwordField.getPassword()), selectedCuisine, String.valueOf(distance), sendHome, address);
        showLoading();
    }

    private void showLoading() {
        logoutCallback.showLoadingScreen();
    }

    void showRestaurants(java.util.List<Restaurant> restaurants) {
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
        JLabel nameLabel = new JLabel(restaurant.getRestaurantActualName());
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

    void showMenu(java.util.List<Map<String, Object>> menu) {
        // Clear the main content panel to show the menu
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        showPlaceOrder(); // Show the search panel again

        // Create a panel to list the menu items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));  // Ensure vertical alignment

        for (Map<String, Object> menuItem : menu) {
            if (menuItem.get("available").equals(false)) {
                continue;  // Skip unavailable items
            }
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

    private void proceedToCheckout(java.util.List<Map<String, Object>> menu) {
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
    private void placeOrder(double totalAmount, String address, String cardNumber, String expirationDate, String cvv, boolean sendHome, boolean useSavedCard, String note, java.util.List<Map<String, Object>> menu) {
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
    void showOrderHistory() {
        clientApp.getOrdersHistoryAsync(usernameField.getText(), new String(passwordField.getPassword()));
        showLoading();
    }

    void showOrderHistoryFrame(List<Order> orders){
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        JPanel orderPanel = new JPanel();
        orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));

        for (Order order : orders) {
            JPanel orderItemPanel = createOrderPanel(order);
            orderPanel.add(orderItemPanel);
            orderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scrollPane = new JScrollPane(orderPanel);

        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private JPanel createOrderPanel(Order order) {
        JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Order ID on the Left
        JLabel orderIdLabel = new JLabel("Order ID: " + order.getOrderId());
        orderPanel.add(orderIdLabel, BorderLayout.WEST);

        // Order Details in the Center
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Restaurant Name with Icon
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel restaurantIconLabel = new JLabel(loadImageIcon("icons/restaurant.png", 24, 24));
        infoPanel.add(restaurantIconLabel, gbc);

        gbc.gridx = 1;
        JLabel restaurantLabel = new JLabel(order.getRestaurantName());
        restaurantLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(restaurantLabel, gbc);

        // Row 1: Total Amount with Icon
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel totalIconLabel = new JLabel(loadImageIcon("icons/total.png", 24, 24));
        infoPanel.add(totalIconLabel, gbc);

        gbc.gridx = 1;
        JLabel totalLabel = new JLabel(String.format("$%.2f", order.getTotalPrice()));
        totalLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        infoPanel.add(totalLabel, gbc);

        // Row 2: Order Date with Icon
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel dateIconLabel = new JLabel(loadImageIcon("icons/date.png", 24, 24));
        infoPanel.add(dateIconLabel, gbc);

        gbc.gridx = 1;
        JLabel dateLabel = new JLabel(String.valueOf(order.getOrderDate()));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        infoPanel.add(dateLabel, gbc);

        // Add the infoPanel to the orderPanel center
        orderPanel.add(infoPanel, BorderLayout.CENTER);

        // Order Status on the Right
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel statusLabel = new JLabel(order.getStatus());
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusPanel.add(statusLabel);

        orderPanel.add(statusPanel, BorderLayout.EAST);

        return orderPanel;
    }

    private void showUserSettings() {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getUserData");
        request.put("username", usernameField.getText());
        request.put("password", new String(passwordField.getPassword()));
        clientApp.addRequest(request);
    }

    // Method to show user settings screen
    private void showUserSettingsFrame() {
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        // Create a panel for the user settings
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add change password button
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        gbc.gridy = 0;
        settingsPanel.add(changePasswordButton, gbc);

        // add change email button
        JButton changeEmailButton = new JButton("Change Email");
        changeEmailButton.addActionListener(e -> showChangeEmailDialog());
        gbc.gridy++;
        settingsPanel.add(changeEmailButton, gbc);

        // Add change phone number button
        JButton changePhoneNumberButton = new JButton("Change Phone Number");
        changePhoneNumberButton.addActionListener(e -> showChangePhoneNumberDialog());
        gbc.gridy++;
        settingsPanel.add(changePhoneNumberButton, gbc);

        // Add change phone number button
        JButton changeCreditCardButton = new JButton("Change Credit Card");
        changeCreditCardButton.addActionListener(e -> showChangeCreditCardDialog());
        gbc.gridy++;
        settingsPanel.add(changeCreditCardButton, gbc);

        // Add change address button
        JButton changeAddressButton = new JButton("Change Address");
        changeAddressButton.addActionListener(e -> showChangeAddressDialog());
        gbc.gridy++;
        settingsPanel.add(changeAddressButton, gbc);

        // Add delete account button
        JButton deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.addActionListener(e -> showDeleteAccountDialog());
        gbc.gridy++;
        settingsPanel.add(deleteAccountButton, gbc);

        mainContentPanel.add(settingsPanel, BorderLayout.CENTER);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();

    }

    void createUserDataPane(Map<String,String> userData){
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        showUserSettingsFrame();

        String username = userData.get("username");
        String email = userData.get("email");
        String phoneNumber = userData.get("phoneNumber");
        String address = userData.get("address");
        String cardNumber = userData.get("creditCardNumber");

        JPanel userDataPanel = new JPanel();
        userDataPanel.setLayout(new GridBagLayout());
        userDataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add username
        gbc.gridx = 0;
        gbc.gridy = 0;
        userDataPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        userDataPanel.add(new JLabel(username), gbc);

        // Add email
        gbc.gridx = 0;
        gbc.gridy = 1;
        userDataPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        userDataPanel.add(new JLabel(email), gbc);

        // Add phone number
        gbc.gridx = 0;
        gbc.gridy = 2;
        userDataPanel.add(new JLabel("Phone Number:"), gbc);

        gbc.gridx = 1;
        userDataPanel.add(new JLabel(phoneNumber), gbc);

        // Add address
        gbc.gridx = 0;
        gbc.gridy = 3;
        userDataPanel.add(new JLabel("Address:"), gbc);

        gbc.gridx = 1;
        userDataPanel.add(new JLabel(address), gbc);

        // Add credit card number
        gbc.gridx = 0;
        gbc.gridy = 4;
        userDataPanel.add(new JLabel("Credit Card Number:"), gbc);

        gbc.gridx = 1;
        userDataPanel.add(new JLabel(cardNumber), gbc);

        mainContentPanel.add(userDataPanel, BorderLayout.NORTH);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void showChangeCreditCardDialog() {
        JDialog dialog = new JDialog(frame, "Change Password", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 200);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Card Number:"), gbc);

        gbc.gridx = 1;
        JTextField cardNumberField = new JTextField(15);
        dialog.add(cardNumberField, gbc);

        // expiration date
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Expiration Date:"), gbc);

        gbc.gridx = 1;
        JTextField expirationDateField = new JTextField(5);
        dialog.add(expirationDateField, gbc);

        // cvv
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("CVV:"), gbc);

        gbc.gridx = 1;
        JTextField cvvField = new JTextField(3);
        dialog.add(cvvField, gbc);

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Change Credit Card");

        submitButton.addActionListener(e -> {
            String cardNumber = cardNumberField.getText();
            String expirationDate = expirationDateField.getText();
            String cvv = cvvField.getText();

            clientApp.updateCreditCardAsync(usernameField.getText(), new String(passwordField.getPassword()), cardNumber, expirationDate, cvv);
            showLoading();
            dialog.dispose();
        });

        dialog.add(submitButton, gbc);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showChangePasswordDialog() {
        JDialog dialog = new JDialog(frame, "Change Password", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 200);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Current password
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Current Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField currentPasswordField = new JPasswordField(15);
        dialog.add(currentPasswordField, gbc);

        // New password
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(15);
        dialog.add(newPasswordField, gbc);

        // Confirm new password
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(15);
        dialog.add(confirmPasswordField, gbc);

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Change Password");
        submitButton.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (newPassword.equals(confirmPassword)) {
                clientApp.changeParameterAsync(usernameField.getText(), currentPassword, "password", newPassword);
                showLoading();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "New passwords do not match!");
            }
        });
        dialog.add(submitButton, gbc);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showChangeEmailDialog() {
        JDialog dialog = new JDialog(frame, "Change Email", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 150);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Current email
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("New Email:"), gbc);

        gbc.gridx = 1;
        JTextField newEmailField = new JTextField(15);
        dialog.add(newEmailField, gbc);

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Change Email");
        submitButton.addActionListener(e -> {
            String newEmail = newEmailField.getText();

            clientApp.changeParameterAsync(usernameField.getText(), new String(passwordField.getPassword()), "email", newEmail);
            showLoading();
            dialog.dispose();
        });
        dialog.add(submitButton, gbc);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showChangePhoneNumberDialog() {
        JDialog dialog = new JDialog(frame, "Change Phone Number", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 150);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // New phone number
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("New Phone Number:"), gbc);

        gbc.gridx = 1;
        JTextField newPhoneNumberField = new JTextField(15);
        dialog.add(newPhoneNumberField, gbc);

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Change Phone Number");
        submitButton.addActionListener(e -> {
            String newPhoneNumber = newPhoneNumberField.getText();

            clientApp.changeParameterAsync(usernameField.getText(), new String(passwordField.getPassword()), "phoneNumber", newPhoneNumber);
            showLoading();
            dialog.dispose();
        });
        dialog.add(submitButton, gbc);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showChangeAddressDialog() {
        JDialog dialog = new JDialog(frame, "Change Address", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 150);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // New address
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("New Address:"), gbc);

        gbc.gridx = 1;
        JTextField newAddressField = new JTextField(15);
        dialog.add(newAddressField, gbc);

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Change Address");
        submitButton.addActionListener(e -> {
            String newAddress = newAddressField.getText();
            clientApp.changeParameterAsync(usernameField.getText(), new String(passwordField.getPassword()), "address", newAddress);
            showLoading();
            // Close the dialog after submitting
            dialog.dispose();
        });
        dialog.add(submitButton, gbc);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showDeleteAccountDialog() {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete your account? This action cannot be undone.",
                "Delete Account",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            clientApp.deleteAccountAsync(usernameField.getText(), new String(passwordField.getPassword()));
            showLoading();
        }
    }

}
