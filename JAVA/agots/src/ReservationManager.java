import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ReservationManager {

    public void loadPendingBookRequests(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Reservations WHERE status = 'Pending'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int reservationId = resultSet.getInt("reservation_id");
                int clientId = resultSet.getInt("client_id");
                String date = resultSet.getString("reservation_date");
                String time = resultSet.getString("reservation_time");
                String eventType = resultSet.getString("event_type");
                String status = resultSet.getString("status");
                model.addRow(new Object[]{reservationId, clientId, date, time, eventType, status});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading book requests.");
        }
    }

    public boolean updateReservationStatus(int reservationId, String status) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE Reservations SET status = ? WHERE reservation_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, status); // Set the new status (Approved or Re jected)
            statement.setInt(2, reservationId); // Set the reservation ID to update
            statement.executeUpdate(); // Execute the update
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for debugging
            JOptionPane.showMessageDialog(null, "Error updating reservation status."); // Show error message
            return false;
        }
    }
}