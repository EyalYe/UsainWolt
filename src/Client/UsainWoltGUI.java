package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class UsainWoltGUI {
    private JFrame frame;
    private ClientApp clientApp;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public UsainWoltGUI(ClientApp clientApp) {
        this.clientApp = clientApp;
        initialize();
    }

    private void initialize() {
        // Setup the main frame
        frame = new JFrame("Usain Wolt");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Call the method to generate the login screen
        generateLogin();

        // Set the frame visible at the end
        frame.setVisible(true);
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
        Image resizedImg = img.getScaledInstance(400, 400, Image.SCALE_SMOOTH);
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
        JTextField usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

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
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                try {
                    Map<String, Object> response = clientApp.login(username, password);
                    if ("true".equals(response.get("success"))) {
                        JOptionPane.showMessageDialog(frame, "Login successful!");
                        // Proceed to the next step after successful login
                    } else {
                        JOptionPane.showMessageDialog(frame, "Login failed: " + response.get("message"));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }
        });

        // Add action listener for signup button
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSignup(); // Call the method to generate the signup screen
            }
        });

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
        Image resizedImg = img.getScaledInstance(400, 400, Image.SCALE_SMOOTH);
        icon = new ImageIcon(resizedImg);

        JLabel imageLabel = new JLabel(icon);
        frame.add(imageLabel, BorderLayout.WEST);

        // Create signup panel
        JPanel signupPanel = new JPanel();
        signupPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create dropdown for selecting user type
        JLabel userTypeLabel = new JLabel("User Type:");
        JComboBox<String> userTypeDropdown = new JComboBox<>(new String[]{"Restaurant", "Customer", "Delivery"});

        // Add UserType label and dropdown
        gbc.gridx = 0;
        gbc.gridy = 0;
        signupPanel.add(userTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        signupPanel.add(userTypeDropdown, gbc);

        // Add Back to Login button
        JButton backButton = new JButton("Back to Login");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        signupPanel.add(backButton, gbc);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateLogin(); // Go back to the login screen
            }
        });

        frame.add(signupPanel, BorderLayout.EAST);
        frame.revalidate();
        frame.repaint();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClientApp clientApp = new ClientApp("localhost", 12345);
                new UsainWoltGUI(clientApp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
