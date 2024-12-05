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

public class AdminFrame extends JFrame {

    public AdminFrame(ManagementFrame previousFrame) {
        setTitle("Admin Login");
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
            if (validateAdmin(username, password)) {
                JOptionPane.showMessageDialog(this, "Admin login successful!");
                dispose();
                new AdminDashboard(); // Show the admin dashboard
            } else {
                JOptionPane.showMessageDialog(this, "Invalid admin credentials.");
            }
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
        add(backButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private boolean validateAdmin(String username, String password) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // Returns true if a record is found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
