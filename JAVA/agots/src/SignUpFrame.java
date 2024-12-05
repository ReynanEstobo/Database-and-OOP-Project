import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class SignUpFrame extends JFrame {

    public SignUpFrame(LandingFrame previousFrame) {
        setTitle("Sign Up");
        setSize(400, 450);
        setLayout(null);
        setResizable(false);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(50, 50, 100, 30);
        JTextField nameField = new JTextField();
        nameField.setBounds(150, 50, 200, 30);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 100, 100, 30);
        JTextField usernameField = new JTextField();
        usernameField.setBounds(150, 100, 200, 30);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 150, 100, 30);
        JPasswordField PasswordField = new JPasswordField();
        PasswordField.setBounds(150, 150, 200, 30);

        JLabel contactLabel = new JLabel("Contact:");
        contactLabel.setBounds(50, 200, 100, 30);
        JTextField contactField = new JTextField();
        contactField.setBounds(150, 200, 200, 30);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setBounds(50, 250, 100, 30);
        JTextField addressField = new JTextField();
        addressField.setBounds(150, 250, 200, 30);

        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setBounds(50, 300, 100, 30);
        signUpButton.addActionListener(e -> {
            String fullName = nameField.getText();
            String username = usernameField.getText();
            String password = new String(PasswordField.getPassword());
            String contact = contactField.getText();
            String address = addressField.getText();

            if (signUpClient(fullName, username, password, contact, address)) {
                JOptionPane.showMessageDialog(this, "Account created successfully!");
                dispose();
                previousFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Error creating account. Please try again.");
            }
        });

        JButton backButton = new JButton("Back");
        backButton.setBounds(270, 300, 100, 30);
        backButton.addActionListener(e -> {
            dispose();
            previousFrame.setVisible(true);
        });

        add(nameLabel);
        add(nameField);
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(PasswordField);
        add(contactLabel);
        add(contactField);
        add(addressLabel);
        add(addressField);
        add(signUpButton);
        add(backButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private boolean signUpClient(String fullName, String username, String password, String contact, String address) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO client (full_name, username, password, contact, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, fullName);
            statement.setString(2, username);
            statement.setString(3, password);
            statement.setString(4, contact);
            statement.setString(5, address);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}