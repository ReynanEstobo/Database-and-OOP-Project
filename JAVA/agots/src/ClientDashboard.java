import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class ClientDashboard extends JFrame {
    private int clientId;

    public ClientDashboard(int clientId, String username) {
        this.clientId = clientId;
        setTitle("Client Dashboard");
        setSize(600, 400); // Increased size
        setLayout(null);
        setResizable(false);

        // Welcome Label
        JLabel welcomeLabel = new JLabel("WELCOME " + username.toUpperCase(), JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(30, 40, 50));
        welcomeLabel.setBounds(0, 30, 600, 40); // Adjusted width
        add(welcomeLabel);

        // Adjusted button sizes and positions
        JButton bookReservationButton = new JButton("Book Reservation");
        bookReservationButton.setBounds(200, 100, 200, 40); // Increased height
        bookReservationButton.addActionListener(e -> bookReservation());
        add(bookReservationButton);

        JButton viewReservationButton = new JButton("View Existing Reservations");
        viewReservationButton.setBounds(200, 160, 200, 40); // Increased height
        viewReservationButton.addActionListener(e -> viewReservations());
        add(viewReservationButton);

        JButton viewPendingRequestsButton = new JButton("View Pending Requests");
        viewPendingRequestsButton.setBounds(200, 220, 200, 40); // Increased height
        viewPendingRequestsButton.addActionListener(e -> viewPendingRequests());
        add(viewPendingRequestsButton);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setBounds(200, 280, 200, 40); // Positioning the button
        logoutButton.addActionListener(e -> logout());
        add(logoutButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void logout() {
        int response = JOptionPane.showConfirmDialog(this, "Do you really want to log out?", "Confirm Log Out",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            this.dispose(); // Close the AdminDashboard
            new LandingFrame(); // Replace with the actual constructor of your landing frame
        }
    }


    private void bookReservation() {
        JFrame bookingFrame = new JFrame("Book Reservation");
        bookingFrame.setSize(400, 400);
        bookingFrame.setLayout(null);
        bookingFrame.setResizable(false);
    
        JLabel dateLabel = new JLabel("Enter Date (YYYY-MM-DD):");
        dateLabel.setBounds(50, 20, 200, 30);
        bookingFrame.add(dateLabel);
    
        JTextField dateField = new JTextField();
        dateField.setBounds(250, 20, 100, 30);
        bookingFrame.add(dateField);
    
        JLabel timeLabel = new JLabel("Enter Time (e.g., 15:00):");
        timeLabel.setBounds(50, 70, 200, 30);
        bookingFrame.add(timeLabel);
    
        JTextField timeField = new JTextField();
        timeField.setBounds(250, 70, 100, 30);
        bookingFrame.add(timeField);
    
        JLabel eventTypeLabel = new JLabel("Event Type:");
        eventTypeLabel.setBounds(50, 120, 100, 30);
        bookingFrame.add(eventTypeLabel);
    
        JTextField eventTypeField = new JTextField();
        eventTypeField.setBounds(150, 120, 200, 30);
        bookingFrame.add(eventTypeField);
    
        JLabel guestCountLabel = new JLabel("Guest Count:");
        guestCountLabel.setBounds(50, 170, 100, 30);
        bookingFrame.add(guestCountLabel);
    
        JTextField guestCountField = new JTextField();
        guestCountField.setBounds(150, 170, 200, 30);
        bookingFrame.add(guestCountField);
    
        JLabel placeLabel = new JLabel("Event Place:");
        placeLabel.setBounds(50, 220, 100, 30);
        bookingFrame.add(placeLabel);
    
        JTextField placeField = new JTextField();
        placeField.setBounds(150, 220, 200, 30);
        bookingFrame.add(placeField);
    
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setBounds(150, 280, 100, 30);
        confirmButton.addActionListener(e -> {
            String date = dateField.getText();
            String time = timeField.getText();
            String eventType = eventTypeField.getText();
            String guestCount = guestCountField.getText();
            String place = placeField.getText();
    
            if (!date.isEmpty() && !time.isEmpty() && !eventType.isEmpty() && !guestCount.isEmpty() && !place.isEmpty()) {
                // Check for existing reservations at the same date and time
                if (isDateTimeAvailable(date, time)) {
                    // Save reservation to the database
                    if (saveReservation(clientId, date, time, eventType, guestCount, place)) {
                        JOptionPane.showMessageDialog(bookingFrame, "Your reservation request has been submitted. Please wait for admin confirmation.");
                    } else {
                        JOptionPane.showMessageDialog(bookingFrame, "Error saving reservation. Please try again.");
                    }
                    bookingFrame.dispose(); // Close the booking frame
                } else {
                    JOptionPane.showMessageDialog(bookingFrame, "The selected date and time are already occupied. Please choose another.");
                }
            } else {
                JOptionPane.showMessageDialog(bookingFrame, "Please fill in all fields.");
            }
        });
        bookingFrame.add(confirmButton);
        bookingFrame.setLocationRelativeTo(null);
        bookingFrame.setVisible(true);
    }

    private boolean isDateTimeAvailable(String date, String time) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM Reservations WHERE reservation_date = ? AND reservation_time = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, date);
            statement.setString(2, time);
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                return resultSet.getInt(1) == 0; // No existing reservations found
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default to unavailable on error
    }

    private boolean saveReservation(int clientId, String date, String time, String eventType, String guestCount, String place) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Reservations (client_id, reservation_date, reservation_time, event_type, guest_count, place, status) VALUES (?, ?, ?, ?, ?, ?, 'Pending')";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clientId);
            statement.setString(2, date);
            statement.setString(3, time);
            statement.setString(4, eventType);
            statement.setString(5, guestCount);
            statement.setString(6, place);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void viewReservations() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT reservation_id, reservation_date, reservation_time, event_type, guest_count, place, status, rejection_reason " +
                           "FROM Reservations WHERE client_id = ? AND (status = 'Approved' OR status = 'Rejected')";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clientId);
            ResultSet resultSet = statement.executeQuery();

            JTextArea reservationsArea = new JTextArea();
            reservationsArea.setEditable(false);
            reservationsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            StringBuilder reservations = new StringBuilder("Your Reservations:\n\n");
            boolean hasReservations = false;

            while (resultSet.next()) {
                hasReservations = true;
                reservations.append("Reservation ID: ").append(resultSet.getInt("reservation_id"))
                        .append("\nDate: ").append(resultSet.getString("reservation_date"))
                        .append("\nTime: ").append(resultSet.getString("reservation_time"))
                        .append("\nEvent Type: ").append(resultSet.getString("event_type"))
                        .append("\nGuest Count: ").append(resultSet.getString("guest_count"))
                        .append("\nPlace: ").append(resultSet.getString("place"))
                        .append("\nStatus: ").append(resultSet.getString("status"));

                // Include rejection reason if the status is 'Rejected'
                if ("Rejected".equals(resultSet.getString("status"))) {
                    reservations.append("\nRejection Reason: ").append(resultSet.getString("rejection_reason"));
                }

                reservations.append("\n-----------------------------------\n"); // Separator
            }

            if (!hasReservations) {
                reservations.append("No existing reservations found.");
            }

            reservationsArea.setText(reservations.toString());
            JScrollPane scrollPane = new JScrollPane(reservationsArea);
            scrollPane.setPreferredSize(new Dimension(350, 200));
            JOptionPane.showMessageDialog(null, scrollPane, "Your Reservations", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching reservations.");
        }
    }

    private void viewPendingRequests() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Reservations WHERE client_id = ? AND status = 'Pending'";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clientId);
            ResultSet resultSet = statement.executeQuery();

            JTextArea pendingRequestsArea = new JTextArea();
            pendingRequestsArea.setEditable(false);
            pendingRequestsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            StringBuilder pendingRequests = new StringBuilder("Your Pending Requests:\n\n");
            boolean hasPendingRequests = false;

            while (resultSet.next()) {
                hasPendingRequests = true;
                String eventType = resultSet.getString("event_type");
                String date = resultSet.getString("reservation_date");
                String time = resultSet.getString("reservation_time");
                String status = resultSet.getString("status");

                pendingRequests.append("Event Type: ").append (eventType)
                        .append("\nDate: ").append(date)
                        .append("\nTime: ").append(time)
                        .append("\nStatus: ").append(status)
                        .append("\n-----------------------------------\n"); // Separator
            }

            if (!hasPendingRequests) {
                pendingRequests.append("No pending requests found.");
            }

            pendingRequestsArea.setText(pendingRequests.toString());
            JScrollPane scrollPane = new JScrollPane(pendingRequestsArea);
            scrollPane.setPreferredSize(new Dimension(350, 200));
            JOptionPane.showMessageDialog(null, scrollPane, "Your Pending Requests", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching pending requests.");
        }
    }
}