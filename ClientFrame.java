import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ClientFrame extends JFrame {

    public ClientFrame(LandingFrame previousFrame) {
        setTitle("Client Login");
        setSize(400, 300);
        setLayout(null);
        setResizable(false);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 50, 100, 30);
        JTextField usernameField = new JTextField();
        usernameField.setBounds(150, 50, 200, 30);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 100, 100, 30);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(150, 100, 200, 30);

        JButton loginButton = new JButton("Log In");
        loginButton.setBounds(50, 150, 100, 30);
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            int clientId = validateClient(username, password);
            if (clientId != -1) {
                JOptionPane.showMessageDialog(this, "Client login successful!");
                dispose();
                new ClientDashboard(clientId, username); // Show the client dashboard
            } else {
                JOptionPane.showMessageDialog(this, "Invalid client credentials.");
            }
        });

        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setBounds(160, 150, 100, 30);
        signUpButton.addActionListener(e -> {
            dispose();
            new SignUpFrame(previousFrame); // Show sign-up frame
        });

        JButton backButton = new JButton("Back");
        backButton.setBounds(270, 150, 100, 30);
        backButton.addActionListener(e -> {
            dispose();
            previousFrame.setVisible(true);
        });

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);
        add(signUpButton);
        add(backButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private int validateClient(String username, String password) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT client_id FROM client WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("client_id"); // Return the client ID if found
            }
            return -1; // Return -1 if no record is found
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

}