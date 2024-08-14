package Client.GUI;

import Client.network.ClientApp;
import Server.Models.Order;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class DeliveryGUI {
    private final JFrame frame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final ClientApp clientApp;
    private final String[] availableCuisines;
    private final LogoutCallback logoutCallback;

    DeliveryGUI(JFrame frame, JTextField usernameField, JPasswordField passwordField, ClientApp clientApp, String[] availableCuisines , LogoutCallback logoutCallback) {
        this.frame = frame;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.clientApp = clientApp;
        this.availableCuisines = availableCuisines;
        this.logoutCallback = logoutCallback;
    }

    // -------------------------------------- UI for Delivery --------------------------------------
    void generateDeliveryUI() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Create a panel for the sidebar with buttons
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(4, 1, 10, 10));
        sidebar.setPreferredSize(new Dimension(300, frame.getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the sidebar

        // Create the buttons
        JButton viewDeliveriesButton = new JButton("View Deliveries");
        JButton deliveryHistoryButton = new JButton("Delivery History");
        JButton userSettingsButton = new JButton("User Settings");
        JButton logoutButton = new JButton("Logout");

        // Add action listeners for buttons
        viewDeliveriesButton.addActionListener(e -> showAvailableDeliveries());
        deliveryHistoryButton.addActionListener(e -> showDeliveryHistory());
        userSettingsButton.addActionListener(e -> showUserSettings());
        logoutButton.addActionListener(e -> handleLogout());

        // Add buttons to the sidebar panel
        sidebar.add(viewDeliveriesButton);
        sidebar.add(deliveryHistoryButton);
        sidebar.add(userSettingsButton);
        sidebar.add(logoutButton);

        // Create a main content panel where the content will change based on button clicks
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the main content

        // Add a welcome label to the main content panel
        JLabel welcomeLabel = new JLabel("Welcome to Usain Wolt Delivery Panel!", JLabel.CENTER);
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

    private void showUserSettings() {
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

        // Change Password
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        gbc.gridy = 0;
        settingsPanel.add(changePasswordButton, gbc);

        // Change Email
        JButton changeEmailButton = new JButton("Change Email");
        changeEmailButton.addActionListener(e -> showChangeEmailDialog());
        gbc.gridy++;
        settingsPanel.add(changeEmailButton, gbc);

        // Change Address
        JButton changeAddressButton = new JButton("Change Address");
        changeAddressButton.addActionListener(e -> showChangeAddressDialog());
        gbc.gridy++;
        settingsPanel.add(changeAddressButton, gbc);

        // Delete Account
        JButton deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.addActionListener(e -> showDeleteAccountDialog());
        gbc.gridy++;
        settingsPanel.add(deleteAccountButton, gbc);

        // Add settingsPanel to the main content panel
        mainContentPanel.add(settingsPanel, BorderLayout.CENTER);

        // Add Income Label
        JLabel incomeLabel = new JLabel("Income: Loading...");
        gbc.gridy++;
        mainContentPanel.add(incomeLabel, BorderLayout.SOUTH);

        // Request the income data from the server
        clientApp.requestIncomeDataAsync(usernameField.getText(), new String(passwordField.getPassword()), income -> {
            SwingUtilities.invokeLater(() -> incomeLabel.setText("Income: $" + income));
        });

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
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

    private void showLoading() {
        logoutCallback.showLoadingScreen();
    }

    private void showAvailableDeliveries() {
        // Clear the main content panel first
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        // Create a panel for the search criteria (top part)
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Add location label
        JLabel locationLabel = new JLabel("Location:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(locationLabel, gbc);

        // Add location field
        JTextField locationField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(locationField, gbc);

        // Add distance label
        JLabel distanceLabel = new JLabel("Distance:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(distanceLabel, gbc);

        // Add distance slider
        JSlider distanceSlider = new JSlider(0, 30, 10); // 1 to 30 km, default 10 km
        distanceSlider.setMajorTickSpacing(10);
        distanceSlider.setMinorTickSpacing(1);
        distanceSlider.setPaintTicks(true);
        distanceSlider.setPaintLabels(true);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(distanceSlider, gbc);

        // Add search button
        JButton searchButton = new JButton("Search");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 2;  // Span across two rows
        gbc.fill = GridBagConstraints.VERTICAL;
        searchPanel.add(searchButton, gbc);

        // Add Delivery Finished button
        JButton deliveryFinishedButton = new JButton("Delivery Finished");
        deliveryFinishedButton.setEnabled(false); // Initially disabled
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 2;  // Span across two rows
        gbc.fill = GridBagConstraints.VERTICAL;
        searchPanel.add(deliveryFinishedButton, gbc);

        // Add action listener for search button
        searchButton.addActionListener(e -> {
            int distance = distanceSlider.getValue();
            String location = locationField.getText();
            try {
                performDeliverySearch(distance, location);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        // Add action listener for Delivery Finished button
        deliveryFinishedButton.addActionListener(e -> {
            markDeliveryAsFinished();
            deliveryFinishedButton.setEnabled(false); // Disable button after marking as finished
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


    private void performDeliverySearch(int distance, String location) throws Exception {
        clientApp.searchDeliveriesAsync(usernameField.getText(), new String(passwordField.getPassword()), location, String.valueOf(distance));
        showLoading();
    }

    // Function to handle the pickup action for an order
    private void pickUpOrder(Order order, JButton deliveryFinishedButton) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "pickupOrder");
        request.put("username", usernameField.getText());
        request.put("password", new String(passwordField.getPassword()));
        request.put("orderId", String.valueOf(order.getOrderId()));
        clientApp.addRequest(request);
        showLoading();

        // Enable the Delivery Finished button after picking up the order
        SwingUtilities.invokeLater(() -> deliveryFinishedButton.setEnabled(true));
    }

    // Function to handle marking the delivery as finished
    private void markDeliveryAsFinished() {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "markOrderDelivered");
        request.put("username", usernameField.getText());
        request.put("password", new String(passwordField.getPassword()));
        clientApp.addRequest(request);
        showLoading();
    }

    private void showAvailableDeliveriesFrame(List<Order> orders) {
        // Get the main content panel to clear and update
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        showAvailableDeliveries();  // Show the search panel again

        // Create a panel to list the available deliveries
        JPanel deliveriesPanel = new JPanel();
        deliveriesPanel.setLayout(new BoxLayout(deliveriesPanel, BoxLayout.Y_AXIS));  // Ensure vertical alignment

        if (orders.isEmpty()) {
            JLabel noResultsLabel = new JLabel("No deliveries found.");
            noResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            deliveriesPanel.add(noResultsLabel);
        } else {
            for (Order order : orders) {
                JPanel orderPanel = createOrderPanel(order);
                deliveriesPanel.add(orderPanel);
                deliveriesPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between orders
            }
        }

        // Add the deliveries panel to the main content panel
        JScrollPane scrollPane = new JScrollPane(deliveriesPanel);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh the main content panel to display the new UI
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private JPanel createOrderPanel(Order order) {
        JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        orderPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Order ID and Customer Name on the Left
        JLabel orderIdLabel = new JLabel("Order ID: " + order.getOrderId());
        JLabel customerNameLabel = new JLabel("Customer: " + order.getCustomerName());
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        leftPanel.add(orderIdLabel);
        leftPanel.add(customerNameLabel);
        orderPanel.add(leftPanel, BorderLayout.WEST);

        // Order Items in the Center
        JPanel itemsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int itemIndex = 0;
        for (Order.Item item : order.getItems()) {
            if (item.getQuantity() > 0) {
                gbc.gridx = 0;
                gbc.gridy = itemIndex++;
                JLabel itemLabel = new JLabel(item.getName() + " x" + item.getQuantity());
                itemsPanel.add(itemLabel, gbc);
            }
        }

        gbc.gridx = 0;
        gbc.gridy = itemIndex++;
        JLabel noteLabel = new JLabel("Note: " + order.getCustomerNote());
        itemsPanel.add(noteLabel, gbc);

        orderPanel.add(itemsPanel, BorderLayout.CENTER);

        // Ready for Pickup Button on the Right
        JButton pickupButton = new JButton("Pick Up");
        pickupButton.setFont(new Font("Arial", Font.BOLD, 16));
        pickupButton.addActionListener(e -> pickUpOrder(order));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(pickupButton);
        orderPanel.add(buttonPanel, BorderLayout.EAST);

        return orderPanel;
    }

    private void pickUpOrder(Order order) {
        // Send a pickup order request to the server
        Map<String, Object> request = new HashMap<>();
        request.put("type", "pickupOrder");
        request.put("username", usernameField.getText());
        request.put("password", new String(passwordField.getPassword()));
        request.put("orderId", String.valueOf(order.getOrderId()));
        clientApp.addRequest(request);
        showLoading();
    }

    private void showOrderHistory() {
        clientApp.getOrdersHistoryAsync(usernameField.getText(), new String(passwordField.getPassword()));
        showLoading();
    }

    private void showOrderHistoryFrame(List<Order> orders){
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



    private void showDeliveryHistory() {
        clientApp.getOrdersHistoryAsync(usernameField.getText(), new String(passwordField.getPassword()));
        showLoading();
    }

}
