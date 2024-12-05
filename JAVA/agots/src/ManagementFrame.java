import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;

public class ManagementFrame extends JFrame {

    public ManagementFrame(LandingFrame previousFrame) {
        setTitle("Management Login");
        setSize(500, 300);
        setLayout(null);

        setResizable(false);

        // Log-in as Admin Button
        JButton adminButton = createButton("Log-in as Admin", 120, 50, 250, 40);
        adminButton.addActionListener(e -> new AdminFrame(this));
        add(adminButton);

        // Log-in as Staff Button
        JButton staffButton = createButton("Log-in as Staff", 120, 100, 250, 40);
        staffButton.addActionListener(e -> new StaffFrame(this));
        add(staffButton);

        JButton backButton = createButton("Back", 120, 150, 250, 40);
        backButton.addActionListener(e -> {
            dispose();
            previousFrame.setVisible(true);
        });
        add(backButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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