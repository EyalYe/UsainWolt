package Client.GUI;

import Client.network.ClientApp;
import Server.Models.Order;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class RestaurantGUI {
    private final JFrame frame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final ClientApp clientApp;
    private final String[] availableCuisines;
    private final LogoutCallback logoutCallback;
    private Gson gson = new Gson();
    private boolean passwordChanged = false;

    public RestaurantGUI(JFrame frame, JTextField usernameField, JPasswordField passwordField, ClientApp clientApp, String[] availableCuisines , LogoutCallback logoutCallback) {
        this.frame = frame;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.clientApp = clientApp;
        this.availableCuisines = availableCuisines;
        this.logoutCallback = logoutCallback;
    }

    void generateRestaurantUI() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Create a panel for the sidebar with buttons
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(4, 1, 10, 10));
        sidebar.setPreferredSize(new Dimension(200, frame.getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the sidebar

        // Create the buttons
        JButton viewOrdersButton = new JButton("View Orders");
        JButton manageMenuButton = new JButton("Manage Menu");
        JButton updateSettingsButton = new JButton("Update Settings");
        JButton logoutButton = new JButton("Logout");

        // Add action listeners for buttons
        viewOrdersButton.addActionListener(e -> showViewOrders());
        manageMenuButton.addActionListener(e -> showManageMenu());
        updateSettingsButton.addActionListener(e -> showRestaurantSettings());
        logoutButton.addActionListener(e -> handleLogout());

        // Add buttons to the sidebar panel
        sidebar.add(viewOrdersButton);
        sidebar.add(manageMenuButton);
        sidebar.add(updateSettingsButton);
        sidebar.add(logoutButton);

        // Create a main content panel where the content will change based on button clicks
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the main content

        // Add a welcome label to the main content panel
        JLabel welcomeLabel = new JLabel("Welcome to Usain Wolt Restaurant Panel!", JLabel.CENTER);
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
        // Implement the logic to logout the restaurant
        logoutCallback.onLogout();
    }

    void showViewOrders() {
        clientApp.getOrdersHistoryAsync(usernameField.getText(), new String(passwordField.getPassword()));
        showLoading();
    }

    private void showLoading() {
        logoutCallback.showLoadingScreen();
    }

    void showManageMenu() {
        clientApp.getMenuAsync(usernameField.getText());
        showLoading();
    }

    void showRestaurantSettings() {
        if (passwordChanged) {
            JOptionPane.showMessageDialog(frame, "Password changed successfully. Please login again.");
            handleLogout();
            return;
        }
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        // Create a panel for the restaurant settings
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Change Profile Picture
        JButton changeProfilePictureButton = new JButton("Change Profile Picture");
        changeProfilePictureButton.addActionListener(e -> changeProfilePicture());
        gbc.gridy = 0;
        settingsPanel.add(changeProfilePictureButton, gbc);

        // Change Restaurant Name
        JButton changeRestaurantNameButton = new JButton("Change Restaurant Name");
        changeRestaurantNameButton.addActionListener(e -> showChangeRestaurantNameDialog());
        gbc.gridy++;
        settingsPanel.add(changeRestaurantNameButton, gbc);

        // Change Business Phone Number
        JButton changeBusinessPhoneNumberButton = new JButton("Change Business Phone Number");
        changeBusinessPhoneNumberButton.addActionListener(e -> showChangeBusinessPhoneNumberDialog());
        gbc.gridy++;
        settingsPanel.add(changeBusinessPhoneNumberButton, gbc);

        // Change Password
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        gbc.gridy++;
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

        mainContentPanel.add(settingsPanel, BorderLayout.CENTER);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // Method to change the profile picture
    private void changeProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                String fileName = file.getName().toLowerCase();
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp");
            }

            @Override
            public String getDescription() {
                return "Image Files (*.jpg, *.jpeg, *.png, *.bmp)";
            }
        });

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                String encodedImage = Base64.getEncoder().encodeToString(imageBytes);

                Map<String, Object> request = new HashMap<>();
                request.put("type", "uploadProfilePicture");
                request.put("username", usernameField.getText());
                request.put("password", new String(passwordField.getPassword()));
                request.put("profilePicture", encodedImage);

                clientApp.addRequest(request);
                showLoading(); // Show a loading screen while processing
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error reading image file: " + e.getMessage());
            }
        }
    }

    // Method to show dialog for changing restaurant name
    private void showChangeRestaurantNameDialog() {
        JDialog dialog = new JDialog(frame, "Change Restaurant Name", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 150);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("New Restaurant Name:"), gbc);

        gbc.gridx = 1;
        JTextField newRestaurantNameField = new JTextField(20);
        dialog.add(newRestaurantNameField, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Change Name");
        submitButton.addActionListener(e -> {
            String newRestaurantName = newRestaurantNameField.getText();
            clientApp.changeParameterAsync(usernameField.getText(), new String(passwordField.getPassword()), "RestaurantName", newRestaurantName);
            showLoading();
            dialog.dispose();
        });
        dialog.add(submitButton, gbc);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    // Method to show dialog for changing business phone number
    private void showChangeBusinessPhoneNumberDialog() {
        JDialog dialog = new JDialog(frame, "Change Business Phone Number", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 150);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("New Business Phone Number:"), gbc);

        gbc.gridx = 1;
        JTextField newBusinessPhoneNumberField = new JTextField(20);
        dialog.add(newBusinessPhoneNumberField, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Change Phone Number");
        submitButton.addActionListener(e -> {
            String newBusinessPhoneNumber = newBusinessPhoneNumberField.getText();
            clientApp.changeParameterAsync(usernameField.getText(), new String(passwordField.getPassword()), "businessPhoneNumber", newBusinessPhoneNumber);
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

        // Current Password
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Current Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField currentPasswordField = new JPasswordField(15);
        dialog.add(currentPasswordField, gbc);

        // New Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(15);
        dialog.add(newPasswordField, gbc);

        // Confirm New Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Confirm New Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(15);
        dialog.add(confirmPasswordField, gbc);

        // Submit Button
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
                this.passwordChanged = true;
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

        // New Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("New Email:"), gbc);

        gbc.gridx = 1;
        JTextField newEmailField = new JTextField(15);
        dialog.add(newEmailField, gbc);

        // Submit Button
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

    private void showChangeAddressDialog() {
        JDialog dialog = new JDialog(frame, "Change Address", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 150);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // New Address
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("New Address:"), gbc);

        gbc.gridx = 1;
        JTextField newAddressField = new JTextField(15);
        dialog.add(newAddressField, gbc);

        // Submit Button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Change Address");
        submitButton.addActionListener(e -> {
            String newAddress = newAddressField.getText();

            clientApp.changeParameterAsync(usernameField.getText(), new String(passwordField.getPassword()), "address", newAddress);
            showLoading();
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



    public void showOrdersFrame(List<Order> orders) {
        // Clear the main content panel first
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        // Create a panel to list the orders
        JPanel ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));  // Ensure vertical alignment

        if (orders.isEmpty()) {
            JLabel noOrdersLabel = new JLabel("No orders available.");
            noOrdersLabel.setHorizontalAlignment(SwingConstants.CENTER);
            ordersPanel.add(noOrdersLabel);
        } else {
            for (Order order : orders) {
                JPanel orderPanel = createOrderPanel(order);
                ordersPanel.add(orderPanel);
                ordersPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between orders
            }
        }

        // Add the orders panel to the main content panel inside a scroll pane
        JScrollPane scrollPane = new JScrollPane(ordersPanel);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh the main content panel to display the new UI
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private JPanel createOrderPanel(Order order) {
        JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a panel for order details
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Padding around the components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Order ID, Customer Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel orderIdLabel = new JLabel("OrderID: " + order.getOrderId());
        orderIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        detailsPanel.add(orderIdLabel, gbc);

        gbc.gridx = 1;
        JLabel customerNameLabel = new JLabel("Customer: " + order.getCustomerName());
        customerNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        detailsPanel.add(customerNameLabel, gbc);

        // List of items in the order
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));

        for (Order.Item item : order.getItems()) {
            if (item.getQuantity() > 0) {  // Only show items with quantity > 0
                JLabel itemLabel = new JLabel(item.getName() + " x" + item.getQuantity());
                itemLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                itemsPanel.add(itemLabel);
            }
        }
        detailsPanel.add(itemsPanel, gbc);

        // Note
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        if (order.getCustomerNote() != null && !order.getCustomerNote().isEmpty()) {
            JLabel noteLabel = new JLabel("Note: " + order.getCustomerNote());
            noteLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            detailsPanel.add(noteLabel, gbc);
        }

        // Ready for pickup button
        JButton readyForPickupButton = new JButton("Ready for Pickup");
        readyForPickupButton.setFont(new Font("Arial", Font.BOLD, 14));
        readyForPickupButton.addActionListener(e -> markOrderAsReady(order));
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        detailsPanel.add(readyForPickupButton, gbc);

        // Add the details panel to the order panel
        orderPanel.add(detailsPanel, BorderLayout.CENTER);

        return orderPanel;
    }

    // Method to mark the order as ready for pickup (you can customize this)
    private void markOrderAsReady(Order order) {
        // Implement the logic to mark the order as ready for pickup
        // For example, you could send a request to the server here
        Map<String, Object> request = new HashMap<>();
        request.put("type", "markOrderReadyForPickup");
        request.put("username", usernameField.getText());
        request.put("password", new String(passwordField.getPassword()));
        Gson gson = new Gson();
        request.put("order" , gson.toJson(order));
        clientApp.addRequest(request);
        showLoading();
    }


    public void showMenu(Object message) {
        Type type = new TypeToken<List<Order.Item>>(){}.getType();
        List<Order.Item> menu = (ArrayList<Order.Item>) gson.fromJson(gson.toJson(message), new TypeToken<List<Order.Item>>(){}.getType());
        showManageMenuFrame(menu);
    }

    private void showManageMenuFrame(List<Order.Item> menu) {
        // Clear the main content panel first
        JPanel mainContentPanel = (JPanel) frame.getContentPane().getComponent(1);
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());

        // Create a panel to list the menu items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));  // Ensure vertical alignment

        for (Order.Item menuItem : menu) {
            JPanel menuItemPanel = createMenuItemPanel(menuItem);
            menuPanel.add(menuItemPanel);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between items
        }

        // Add the menu panel to a scroll pane in the main content panel
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Add an "Add Item" button at the bottom
        JButton addItemButton = new JButton("Add Item");
        addItemButton.setFont(new Font("Arial", Font.BOLD, 16));
        addItemButton.setPreferredSize(new Dimension(200, 50));
        addItemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addItemButton.addActionListener(e -> addNewItem());  // Implement addNewItem method to add new items

        JPanel addButtonPanel = new JPanel();
        addButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        addButtonPanel.add(addItemButton);

        mainContentPanel.add(addButtonPanel, BorderLayout.SOUTH);

        // Refresh the main content panel to display the new UI
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private JPanel createMenuItemPanel(Order.Item menuItem) {
        JPanel menuItemPanel = new JPanel(new BorderLayout(10, 10));
        menuItemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Item Image on the Left
        JLabel itemImageLabel = new JLabel(loadImageIcon(menuItem.getPhotoUrl(), 100, 100));
        menuItemPanel.add(itemImageLabel, BorderLayout.WEST);

        // Text and Buttons in the Center
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Item Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel(menuItem.getName() + " - $" + menuItem.getPrice());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(nameLabel, gbc);

        // Add the infoPanel to the menuItemPanel center
        menuItemPanel.add(infoPanel, BorderLayout.CENTER);

        // Buttons on the Right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Disable/Enable Button
        JButton toggleAvailabilityButton = new JButton(menuItem.isAvailable() ? "Disable" : "Enable");
        toggleAvailabilityButton.addActionListener(e -> toggleItemAvailability(menuItem, toggleAvailabilityButton));

        // Remove Button
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> removeMenuItem(menuItem));

        // Change Image Button
        JButton changeImageButton = new JButton("Change Image");
        changeImageButton.addActionListener(e -> changeItemImage(menuItem));

        // Add buttons to the button panel
        buttonPanel.add(toggleAvailabilityButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(changeImageButton);

        menuItemPanel.add(buttonPanel, BorderLayout.EAST);

        // Grey out the item if it's not available
        if (!menuItem.isAvailable()) {
            menuItemPanel.setBackground(Color.GRAY);
            menuItemPanel.setEnabled(false);
        }

        return menuItemPanel;
    }

// Implementing the Helper Methods

    // Method to toggle item availability
    private void toggleItemAvailability(Order.Item menuItem, JButton button) {
        menuItem.setAvailable(!menuItem.isAvailable());
        button.setText(menuItem.isAvailable() ? "Disable" : "Enable");
        if (!menuItem.isAvailable()) {
            clientApp.disableItemAsync(usernameField.getText(), new String(passwordField.getPassword()), menuItem.getName());
        } else {
            clientApp.enableItemAsync(usernameField.getText(), new String(passwordField.getPassword()), menuItem.getName());
        }
    }

    private void removeMenuItem(Order.Item menuItem) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("type", "updateMenu");
            request.put("username", usernameField.getText());
            request.put("password", new String(passwordField.getPassword()));
            request.put("restaurantName", usernameField.getText());
            request.put("itemName", menuItem.getName());
            request.put("action", "remove");

            clientApp.addRequest(request);
            showLoading(); // Show a loading screen while processing
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error removing menu item: " + e.getMessage());
        }
    }



    private void changeItemImage(Order.Item menuItem) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a new image for " + menuItem.getName());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                String fileName = file.getName().toLowerCase();
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "Image Files (*.jpg, *.jpeg, *.png)";
            }
        });

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                String encodedImage = Base64.getEncoder().encodeToString(imageBytes);

                Map<String, Object> request = new HashMap<>();
                request.put("type", "updateMenu");
                request.put("username", usernameField.getText());
                request.put("password", new String(passwordField.getPassword()));
                request.put("restaurantName", usernameField.getText());
                request.put("itemName", menuItem.getName());
                request.put("action", "update");
                request.put("price", String.valueOf(menuItem.getPrice()));  // Convert Double to String
                request.put("description", menuItem.getDescription());
                request.put("isAvailable", String.valueOf(menuItem.isAvailable()));  // Convert boolean to String
                request.put("image", encodedImage);

                clientApp.addRequest(request);
                showLoading(); // Show a loading screen while processing
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error reading image file: " + e.getMessage());
            }
        }
    }


    private void addNewItem() {
        JTextField itemNameField = new JTextField(20);
        JTextField itemPriceField = new JTextField(20);
        JTextArea itemDescriptionArea = new JTextArea(5, 20);
        JCheckBox availabilityCheckBox = new JCheckBox("Available", true);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an image for the new item");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                String fileName = file.getName().toLowerCase();
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "Image Files (*.jpg, *.jpeg, *.png)";
            }
        });

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                String encodedImage = Base64.getEncoder().encodeToString(imageBytes);

                JPanel inputPanel = new JPanel(new GridLayout(4, 2));
                inputPanel.add(new JLabel("Item Name:"));
                inputPanel.add(itemNameField);
                inputPanel.add(new JLabel("Price:"));
                inputPanel.add(itemPriceField);
                inputPanel.add(new JLabel("Description:"));
                inputPanel.add(new JScrollPane(itemDescriptionArea));
                inputPanel.add(new JLabel("Available:"));
                inputPanel.add(availabilityCheckBox);

                int option = JOptionPane.showConfirmDialog(frame, inputPanel, "Add New Item", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String itemName = itemNameField.getText();
                    double itemPrice;
                    try {
                        itemPrice = Double.parseDouble(itemPriceField.getText());
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(frame, "Invalid price format. Please enter a valid number.");
                        return;
                    }
                    String itemDescription = itemDescriptionArea.getText();
                    boolean isAvailable = availabilityCheckBox.isSelected();

                    Map<String, Object> request = new HashMap<>();
                    request.put("type", "updateMenu");
                    request.put("username", usernameField.getText());
                    request.put("password", new String(passwordField.getPassword()));
                    request.put("restaurantName", usernameField.getText());
                    request.put("itemName", itemName);
                    request.put("price", String.valueOf(itemPrice));  // Convert Double to String
                    request.put("description", itemDescription != null ? itemDescription : "");  // Handle null description
                    request.put("isAvailable", String.valueOf(isAvailable));  // Convert boolean to String
                    request.put("image", encodedImage != null ? encodedImage : "");  // Handle null image
                    request.put("action", "add");  // Specify action as add

                    clientApp.addRequest(request);
                    showLoading(); // Show a loading screen while processing
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error reading image file: " + e.getMessage());
            }
        }
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

}
