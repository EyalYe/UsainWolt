package Client.GUI;

import Client.network.ClientApp;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LoginPanel {

    private final JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private final ClientApp clientApp;

    LoginPanel(JFrame frame, ClientApp clientApp, JTextField usernameField, JPasswordField passwordField, LogoutCallback logoutCallback) {
        this.frame = frame;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.clientApp = clientApp;
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public void generateLogin() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Add image to the left side
        String imagePath = "icons/LOGO.jpg";  // Update this to the correct path
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
        String imagePath = "icons/LOGO.jpg";  // Update this to the correct path
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
        JTextField signupUsernameField = UsainWoltGUI.addFormField(formPanel, "Username", 0);
        JPasswordField signupPasswordField = (JPasswordField) UsainWoltGUI.addFormField(formPanel, "Password", 1, true);
        JPasswordField confirmPasswordField = (JPasswordField) UsainWoltGUI.addFormField(formPanel, "Confirm Password", 2, true);
        JTextField emailField = UsainWoltGUI.addFormField(formPanel, "Email", 3);
        JTextField addressField = UsainWoltGUI.addFormField(formPanel, "Address", 4);
        JTextField phoneNumberField = UsainWoltGUI.addFormField(formPanel, "Phone Number", 5);

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
            UsainWoltGUI.addFormField(formPanel, "Username", 0, signupUsernameField);
            UsainWoltGUI.addFormField(formPanel, "Password", 1, signupPasswordField, true);
            UsainWoltGUI.addFormField(formPanel, "Confirm Password", 2, confirmPasswordField, true);
            UsainWoltGUI.addFormField(formPanel, "Email", 3, emailField);
            UsainWoltGUI.addFormField(formPanel, "Address", 4, addressField);
            UsainWoltGUI.addFormField(formPanel, "Phone Number", 5, phoneNumberField);

            String userType = (String) userTypeDropdown.getSelectedItem();
            if ("Restaurant".equals(userType)) {
                UsainWoltGUI.addFormField(formPanel, "Business Phone", 6, businessPhoneField);
                UsainWoltGUI.addComboBoxField(formPanel, "Cuisine", 7, cuisineDropdown);
                gbc.gridy = 8;
            } else if ("Delivery".equals(userType)) {
                UsainWoltGUI.addFormField(formPanel, "Acceptance Token", 6, tokenField);
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

}
