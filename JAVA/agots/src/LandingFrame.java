import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class LandingFrame extends JFrame {

    public LandingFrame() {
        setTitle("Landing Page");
        setSize(800, 400); // Frame size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        // Welcome Label
        JLabel welcomeLabel = new JLabel("WELCOME", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 48)); // Font size
        welcomeLabel.setForeground(new Color(30, 40, 50));
        welcomeLabel.setBounds(0, 50, 800, 50); // Adjusted height and position
        add(welcomeLabel);

        // Title Label
        JLabel titleLabel = new JLabel("AGOT'S Restaurant and Catering Service", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24)); // Font size
        titleLabel.setForeground(new Color(80, 100, 120));
        titleLabel.setBounds(0, 120, 800, 30); // Adjusted position
        add(titleLabel);

        // Log-in as Management Button
        JButton managementButton = createButton("Log-in as Management", 275, 180, 250, 50); // Adjusted position
        managementButton.addActionListener(e -> new ManagementFrame(this));
        add(managementButton);

        // Log-in as Client Button
        JButton clientButton = createButton("Log-in as Client", 275, 250, 250, 50); // Adjusted position
        clientButton.addActionListener(e -> new ClientFrame(this));
        add(clientButton);

        // Center the frame on the screen
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 18)); // Font size
        button.setBackground(new Color(0, 180, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(null);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 150, 220));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 180, 255));
            }
        });

        return button;
    }
}