package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class CustomerGUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ClientApp clientApp;
    private JFrame signupFrame;
    private String username;
    private String password;

    public CustomerGUI(ClientApp clientApp) {
        this.clientApp = clientApp;
        initialize();
    }

    private void initialize() {
        // Set up the frame with a modern look and dark mode
        frame = new JFrame("Customer Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Dark mode colors
        Color backgroundColor = new Color(45, 45, 45);
        Color textColor = new Color(230, 230, 230);
        Color buttonColor = new Color(70, 70, 70);
        Color buttonTextColor = new Color(200, 200, 200);

        // Panel setup with BoxLayout
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Center the panel
        JPanel centeredPanel = new JPanel();
        centeredPanel.setBackground(backgroundColor);
        centeredPanel.setLayout(new GridBagLayout());

        // Create the form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(buttonColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Remove the unwanted border

        // Add components to the form panel
        placeComponents(formPanel, backgroundColor, textColor, buttonColor, buttonTextColor);

        // Add the form panel to the center
        centeredPanel.add(formPanel);
        panel.add(centeredPanel);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel, Color backgroundColor, Color textColor, Color buttonColor, Color buttonTextColor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);  // Padding around components

        // Username Label
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userLabel, gbc);

        // Username Field
        usernameField = new JTextField(20);
        usernameField.setBackground(buttonColor);
        usernameField.setForeground(textColor);
        usernameField.setCaretColor(textColor);
        usernameField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(usernameField, gbc);

        // Password Label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(passwordLabel, gbc);

        // Password Field
        passwordField = new JPasswordField(20);
        passwordField.setBackground(buttonColor);
        passwordField.setForeground(textColor);
        passwordField.setCaretColor(textColor);
        passwordField.setBorder(BorderFactory.createLineBorder(textColor));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(buttonColor);
        loginButton.setForeground(buttonTextColor);
        loginButton.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(loginButton, gbc);

        // Signup Button
        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(buttonColor);
        signupButton.setForeground(buttonTextColor);
        signupButton.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(signupButton, gbc);

        // Action listeners for buttons
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
            Map<String, String> response = clientApp.login(username, password);
            if ("true".equals(response.get("success"))) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                openMainMenu();
                this.username = username;
                this.password = password;
                frame.dispose();  // Close the login window
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
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Ensure no border

        placeSignupComponents(formPanel, backgroundColor, textColor, buttonColor, buttonTextColor);
        centeredPanel.add(formPanel);
        panel.add(centeredPanel);

        signupFrame.add(panel);
        signupFrame.setVisible(true);
    }

    private void placeSignupComponents(JPanel panel, Color backgroundColor, Color textColor, Color buttonColor, Color buttonTextColor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);  // Padding around components

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
                    Map<String, String> response = clientApp.signupCustomer(username, password, address, phone, email);
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

        // Create the tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // User Name Tab
        JPanel userPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Add buttons to userPanel
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

        // Add user panel to tab
        tabbedPane.addTab(usernameField.getText(), userPanel);

        // Place Order Tab
        JPanel placeOrderPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Cuisine Label and Dropdown
        JLabel cuisineLabel = new JLabel("Select Cuisine:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        placeOrderPanel.add(cuisineLabel, gbc);

        JComboBox<String> cuisineDropdown = new JComboBox<>(fetchAvailableCuisines());
        gbc.gridx = 0;
        gbc.gridy = 1;
        placeOrderPanel.add(cuisineDropdown, gbc);

        // Distance Label and Dropdown
        JLabel distanceLabel = new JLabel("Select Distance:");
        gbc.gridx = 1;
        gbc.gridy = 0;
        placeOrderPanel.add(distanceLabel, gbc);

        JComboBox<String> distanceDropdown = new JComboBox<>(new String[] {
                "5km", "10km", "15km", "20km", "25km", "30km"
        });
        gbc.gridx = 1;
        gbc.gridy = 1;
        placeOrderPanel.add(distanceDropdown, gbc);

        // Search Button
        JButton searchButton = new JButton("Search");
        gbc.gridx = 2;
        gbc.gridy = 1;
        placeOrderPanel.add(searchButton, gbc);

        // Add place order panel to tab
        tabbedPane.addTab("Place Order", placeOrderPanel);

        // Add the tabbed pane to the frame
        mainMenuFrame.add(tabbedPane);
        mainMenuFrame.setVisible(true);
    }


    private String[] fetchAvailableCuisines() {
        try {
            // Send request to the server
            Map<String, String> response = clientApp.getAvailableCuisines();

            // Check if the request was successful
            if ("true".equals(response.get("success"))) {
                String cuisinesStr = response.get("message");
                return cuisinesStr.split(","); // Assuming cuisines are returned as a comma-separated string
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fallback if the server request fails
        return new String[] {"Select Cuisine"};
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
