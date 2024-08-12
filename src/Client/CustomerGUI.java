package Client;

import Server.Order;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class CustomerGUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ClientApp clientApp;
    private JFrame signupFrame;
    private String username;
    private String password;
    private DefaultListModel<Restaurant> restaurantListModel;
    private JList<Restaurant> restaurantList;

    public class RestaurantListCellRenderer extends JPanel implements ListCellRenderer<Restaurant> {
        private JLabel imageLabel;
        private JLabel nameLabel;

        public RestaurantListCellRenderer() {
            setLayout(new BorderLayout(10, 10));
            imageLabel = new JLabel();
            nameLabel = new JLabel();
            nameLabel.setForeground(Color.WHITE); // Adjust for dark mode if necessary
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            add(imageLabel, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Restaurant> list, Restaurant restaurant, int index, boolean isSelected, boolean cellHasFocus) {
            // Set the text to show restaurant name, distance, etc.
            nameLabel.setText(restaurant.toString());

            // Load the profile picture
            if (restaurant.getProfilePictureUrl() != null) {
                try {
                    // Load the image from the URL
                    ImageIcon icon = new ImageIcon(new URL(restaurant.getProfilePictureUrl()));
                    Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                } catch (Exception e) {
                    imageLabel.setIcon(null); // Fallback if the image fails to load
                }
            } else {
                imageLabel.setIcon(null); // No image available
            }

            // Highlight selected item
            setBackground(isSelected ? Color.DARK_GRAY : Color.BLACK);
            return this;
        }
    }

    public CustomerGUI(ClientApp clientApp) {
        this.clientApp = clientApp;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Customer Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Dark mode colors
        Color backgroundColor = new Color(45, 45, 45);
        Color textColor = new Color(230, 230, 230);
        Color buttonColor = new Color(70, 70, 70);
        Color buttonTextColor = new Color(200, 200, 200);

        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel centeredPanel = new JPanel();
        centeredPanel.setBackground(backgroundColor);
        centeredPanel.setLayout(new GridBagLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(buttonColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        placeComponents(formPanel, backgroundColor, textColor, buttonColor, buttonTextColor);

        centeredPanel.add(formPanel);
        panel.add(centeredPanel);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel, Color backgroundColor, Color textColor, Color buttonColor, Color buttonTextColor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setBackground(buttonColor);
        usernameField.setForeground(textColor);
        usernameField.setCaretColor(textColor);
        usernameField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setBackground(buttonColor);
        passwordField.setForeground(textColor);
        passwordField.setCaretColor(textColor);
        passwordField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(buttonColor);
        loginButton.setForeground(buttonTextColor);
        loginButton.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(loginButton, gbc);

        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(buttonColor);
        signupButton.setForeground(buttonTextColor);
        signupButton.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(signupButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSignupForm(backgroundColor, textColor, buttonColor, buttonTextColor);
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            Map<String, Object> response = clientApp.login(username, password);
            if ("true".equals(response.get("success"))) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                this.username = username;
                this.password = password;
                openMainMenu();
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Login failed: " + response.get("message"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void openSignupForm(Color backgroundColor, Color textColor, Color buttonColor, Color buttonTextColor) {
        signupFrame = new JFrame("Customer Signup");
        signupFrame.setSize(400, 400);
        signupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel centeredPanel = new JPanel();
        centeredPanel.setBackground(backgroundColor);
        centeredPanel.setLayout(new GridBagLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(buttonColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        placeSignupComponents(formPanel, backgroundColor, textColor, buttonColor, buttonTextColor);
        centeredPanel.add(formPanel);
        panel.add(centeredPanel);

        signupFrame.add(panel);
        signupFrame.setVisible(true);
    }

    private void placeSignupComponents(JPanel panel, Color backgroundColor, Color textColor, Color buttonColor, Color buttonTextColor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userLabel, gbc);

        JTextField signupUsernameField = new JTextField(20);
        signupUsernameField.setBackground(buttonColor);
        signupUsernameField.setForeground(textColor);
        signupUsernameField.setCaretColor(textColor);
        signupUsernameField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(signupUsernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(passwordLabel, gbc);

        JPasswordField signupPasswordField = new JPasswordField(20);
        signupPasswordField.setBackground(buttonColor);
        signupPasswordField.setForeground(textColor);
        signupPasswordField.setCaretColor(textColor);
        signupPasswordField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(signupPasswordField, gbc);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(addressLabel, gbc);

        JTextField addressField = new JTextField(20);
        addressField.setBackground(buttonColor);
        addressField.setForeground(textColor);
        addressField.setCaretColor(textColor);
        addressField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(addressField, gbc);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(phoneLabel, gbc);

        JTextField phoneField = new JTextField(20);
        phoneField.setBackground(buttonColor);
        phoneField.setForeground(textColor);
        phoneField.setCaretColor(textColor);
        phoneField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(phoneField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        emailField.setBackground(buttonColor);
        emailField.setForeground(textColor);
        emailField.setCaretColor(textColor);
        emailField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(emailField, gbc);

        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(buttonColor);
        signupButton.setForeground(buttonTextColor);
        signupButton.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        panel.add(signupButton, gbc);

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = signupUsernameField.getText();
                String password = new String(signupPasswordField.getPassword());
                String address = addressField.getText();
                String phone = phoneField.getText();
                String email = emailField.getText();

                try {
                    Map<String, Object> response = clientApp.signupCustomer(username, password, address, phone, email);
                    if ("true".equals(response.get("success"))) {
                        JOptionPane.showMessageDialog(panel, "Signup successful! Please log in.");
                        signupFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(panel, "Signup failed: " + response.get("message"));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                }
            }
        });
    }

    private void openMainMenu() {
        JFrame mainMenuFrame = new JFrame("Customer Main Menu");
        mainMenuFrame.setSize(400, 400);
        mainMenuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel userPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JButton changePasswordButton = new JButton("Change Password");
        gbc.gridx = 0;
        gbc.gridy = 0;
        userPanel.add(changePasswordButton, gbc);

        JButton changeEmailButton = new JButton("Change Email");
        gbc.gridy = 1;
        userPanel.add(changeEmailButton, gbc);

        JButton getOrderHistoryButton = new JButton("Get Order History");
        gbc.gridy = 2;
        userPanel.add(getOrderHistoryButton, gbc);

        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleChangePassword();
            }
        });

        changeEmailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleChangeEmail();
            }
        });

        getOrderHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleGetOrderHistory();
            }
        });

        tabbedPane.addTab(username, userPanel);

        JPanel placeOrderPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel cuisineLabel = new JLabel("Select Cuisine:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        placeOrderPanel.add(cuisineLabel, gbc);

        JComboBox<String> cuisineDropdown = new JComboBox<>(fetchAvailableCuisines());
        gbc.gridx = 0;
        gbc.gridy = 1;
        placeOrderPanel.add(cuisineDropdown, gbc);

        JLabel distanceLabel = new JLabel("Select Distance:");
        gbc.gridx = 1;
        gbc.gridy = 0;
        placeOrderPanel.add(distanceLabel, gbc);

        JComboBox<String> distanceDropdown = new JComboBox<>(new String[]{
                "5km", "10km", "15km", "20km", "25km", "30km"
        });
        gbc.gridx = 1;
        gbc.gridy = 1;
        placeOrderPanel.add(distanceDropdown, gbc);

        JButton searchButton = new JButton("Search");
        gbc.gridx = 2;
        gbc.gridy = 1;
        placeOrderPanel.add(searchButton, gbc);

        restaurantListModel = new DefaultListModel<>();
        restaurantList = new JList<>(restaurantListModel);
        restaurantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        restaurantList.setVisibleRowCount(10);
        restaurantList.setCellRenderer(new RestaurantListCellRenderer()); // Use the custom renderer
        JScrollPane listScrollPane = new JScrollPane(restaurantList);
        listScrollPane.setPreferredSize(new Dimension(350, 150));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        placeOrderPanel.add(listScrollPane, gbc);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch(cuisineDropdown, distanceDropdown);
            }
        });

        restaurantList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                Restaurant selectedRestaurant = restaurantList.getSelectedValue();
                if (selectedRestaurant != null) {
                    showRestaurantMenu(selectedRestaurant);
                }
            }
        });

        tabbedPane.addTab("Place Order", placeOrderPanel);

        mainMenuFrame.add(tabbedPane);
        mainMenuFrame.setVisible(true);
    }


    private void handleSearch(JComboBox<String> cuisineDropdown, JComboBox<String> distanceDropdown) {
        try {
            restaurantListModel.clear();

            String selectedCuisine = (String) cuisineDropdown.getSelectedItem();
            String selectedDistance = (String) distanceDropdown.getSelectedItem();

            List<Restaurant> restaurants = clientApp.searchRestaurants(username, password, selectedCuisine, selectedDistance);

            for (Restaurant restaurant : restaurants) {
                restaurantListModel.addElement(restaurant);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    private void showRestaurantMenu(Restaurant restaurant) {
        try {
            List<Order.Item> menuItems = clientApp.getMenu(username, password, restaurant.getName());

            JPanel menuPanel = new JPanel();
            menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

            for (Order.Item item : menuItems) {
                JPanel itemPanel = new JPanel(new BorderLayout());

                // Label for item name and price
                JLabel itemLabel = new JLabel(item.getName() + " - $" + item.getPrice());
                itemPanel.add(itemLabel, BorderLayout.NORTH);

                // Text area for description
                JTextArea descriptionArea = new JTextArea(item.getDescription());
                descriptionArea.setLineWrap(true);
                descriptionArea.setWrapStyleWord(true);
                itemPanel.add(descriptionArea, BorderLayout.CENTER);

                // Image for the menu item
                if (item.getPhotoUrl() != null) {
                    ImageIcon imageIcon = new ImageIcon(new URL( item.getPhotoUrl()));
                    JLabel imageLabel = new JLabel(imageIcon);
                    itemPanel.add(imageLabel, BorderLayout.EAST);
                }

                menuPanel.add(itemPanel);
            }

            JScrollPane scrollPane = new JScrollPane(menuPanel);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(frame, scrollPane, "Menu for " + restaurant.getName(), JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error fetching menu: " + e.getMessage());
        }
    }


    private String[] fetchAvailableCuisines() {
        try {
            Map<String, Object> response = clientApp.getAvailableCuisines();
            if ("true".equals(response.get("success"))) {
                String cuisinesStr = (String) response.get("message");
                return cuisinesStr.split(",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[] {"Select Cuisine"};
    }

    private void handleChangePassword() {
        String newPassword = JOptionPane.showInputDialog(frame, "Enter new password:");
        if (newPassword != null && !newPassword.isEmpty()) {
            try {
                Map<String, Object> response = clientApp.changePassword(username, password, newPassword);
                if ("true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Password changed successfully!");
                    this.password = newPassword;
                } else {
                    JOptionPane.showMessageDialog(frame, "Password change failed: " + response.get("message"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        }
    }

    private void handleChangeEmail() {
        String newEmail = JOptionPane.showInputDialog(frame, "Enter new email:");
        if (newEmail != null && !newEmail.isEmpty()) {
            try {
                Map<String, Object> response = clientApp.changeEmail(username, password, newEmail);
                if ("true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Email changed successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Email change failed: " + response.get("message"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        }
    }

    private void handleGetOrderHistory() {
        try {
            List<Order> orders = clientApp.getOrdersHistory(username, password);

            StringBuilder orderHistory = new StringBuilder();
            for (Order order : orders) {
                orderHistory.append(order.toString()).append("\n\n");
            }

            JTextArea textArea = new JTextArea(orderHistory.toString());
            JScrollPane scrollPane = new JScrollPane(textArea);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            scrollPane.setPreferredSize(new Dimension(350, 200));
            JOptionPane.showMessageDialog(frame, scrollPane, "Order History", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            ClientApp clientApp = new ClientApp("localhost", 12345);
            new CustomerGUI(clientApp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
