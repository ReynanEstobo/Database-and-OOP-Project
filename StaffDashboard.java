import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities; // Import HashSet to store used events

public class StaffDashboard extends JFrame {
    private String selectedEventType; // Class-level variable to store the selected event type
    private HashSet<String> usedEventTypes; // Set to track used event types

    public StaffDashboard() {
        setTitle("Staff Dashboard");
        setSize(500, 400);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        usedEventTypes = new HashSet<>(); // Initialize the set

        JLabel welcomeLabel = new JLabel("Welcome to the Staff Dashboard", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setBounds(0, 30, 500, 40);
        add(welcomeLabel);

        JButton viewApprovedReservationsButton = createButton("View Approved Reservations", 150, 100, 200, 40);
        viewApprovedReservationsButton.addActionListener(e -> viewApprovedReservations());
        add(viewApprovedReservationsButton);

        JButton bringOutItemsButton = createButton("Bring Out Items", 150, 160, 200, 40);
        bringOutItemsButton.addActionListener(e -> bringOutItems());
        add(bringOutItemsButton);

        JButton returnItemsButton = createButton("Return Items", 150, 220, 200, 40);
        returnItemsButton.addActionListener(e -> returnItems());
        add(returnItemsButton);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setBounds(150, 280, 200, 40); // Positioning the button
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

    private JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        return button;
    }

    private void viewApprovedReservations() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT reservation_id, event_type, guest_count, place, reservation_date, reservation_time " +
                    "FROM reservations WHERE status = 'Approved' AND is_completed = FALSE"; // Exclude completed events
            PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery();
    
            DefaultListModel<String> listModel = new DefaultListModel<>();
            while (resultSet.next()) {
                String reservationDetails = "<html><b>Event Type: " + resultSet.getString("event_type") + "</b><br>" +
                        "Place: " + resultSet.getString("place") + "<br>" +
                        "Date: " + resultSet.getString("reservation_date") + "<br>" +
                        "Time: " + resultSet.getString("reservation_time") + "<br>" +
                        "Guests: " + resultSet.getInt("guest_count") + "</html>";
                listModel.addElement(reservationDetails);
            }
    
            JList<String> reservationList = new JList<>(listModel);
            reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane listScrollPane = new JScrollPane(reservationList);
            listScrollPane.setPreferredSize(new Dimension(300, 200));
    
            int option = JOptionPane.showConfirmDialog(null, listScrollPane, "Select Approved Reservation",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION && reservationList.getSelectedIndex() != -1) {
                int selectedIndex = reservationList.getSelectedIndex();
                resultSet.absolute(selectedIndex + 1); // Now this will work
                selectedEventType = resultSet.getString("event_type"); // Ensure this column is present
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching approved reservations: " + e.getMessage());
        }
    }

    private void bringOutItems() {
        // First, ensure the staff selects an event from approved reservations
        if (selectedEventType == null) {
            JOptionPane.showMessageDialog(null, "Please select an approved reservation first.");
            viewApprovedReservations(); // Prompt user to select an event
            if (selectedEventType == null) {
                return; // User canceled or did not select an event
            }
        }
    
        // Add the selected event type to the used events set
        usedEventTypes.add(selectedEventType);
    
        // Show available items in inventory
        JFrame inventoryFrame = new JFrame("Available Items in Inventory");
        inventoryFrame.setSize(400, 400);
        inventoryFrame.setLayout(null);
        inventoryFrame.setResizable(false);
    
        JLabel inventoryLabel = new JLabel("Available Items:");
        inventoryLabel.setBounds(50, 20, 200, 30);
        inventoryFrame.add(inventoryLabel);
    
        DefaultListModel<String> inventoryModel = new DefaultListModel<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String inventoryQuery = "SELECT item_name, SUM(quantity) AS total_quantity FROM Inventory WHERE item_type = 'Inside' AND quantity > 0 GROUP BY item_name";
            PreparedStatement inventoryStatement = connection.prepareStatement(inventoryQuery);
            ResultSet inventoryResultSet = inventoryStatement.executeQuery();
    
            while (inventoryResultSet.next()) {
                String itemDetails = inventoryResultSet.getString("item_name") + " - Quantity: "
                        + inventoryResultSet.getInt("total_quantity");
                inventoryModel.addElement(itemDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(inventoryFrame, "Error fetching inventory: " + e.getMessage());
            return;
        }
    
        JList<String> inventoryList = new JList<>(inventoryModel);
        inventoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane inventoryScrollPane = new JScrollPane(inventoryList);
        inventoryScrollPane.setBounds(50, 60, 300, 200);
        inventoryFrame.add(inventoryScrollPane);
    
        JButton confirmButton = new JButton("Confirm Selection");
        confirmButton.setBounds(150, 280, 150, 30);
        confirmButton.addActionListener(e -> {
            // Get selected items and ask for quantities
            JFrame quantityFrame = new JFrame("Enter Quantities");
            quantityFrame.setSize(400, 300);
            quantityFrame.setLayout(null);
            quantityFrame.setResizable(false);
    
            DefaultListModel<String> quantityModel = new DefaultListModel<>();
            for (String item : inventoryList.getSelectedValuesList()) {
                quantityModel.addElement(item);
            }
    
            JList<String> quantityList = new JList<>(quantityModel);
            JScrollPane quantityScrollPane = new JScrollPane(quantityList);
            quantityScrollPane.setBounds(50, 20, 300, 200);
            quantityFrame.add(quantityScrollPane);
    
            JButton enterQuantitiesButton = new JButton("Enter Quantities");
            enterQuantitiesButton.setBounds(150, 230, 150, 30);
            enterQuantitiesButton.addActionListener(f -> {
                // Get quantities and update inventory
                StringBuilder selectedItems = new StringBuilder();
                for (String item : quantityList.getSelectedValuesList()) {
                    String itemName = item.split(" - ")[0]; // Extract item name
                    int availableQuantity = Integer.parseInt(item.split(": ")[1].trim()); // Get available quantity
                    int quantity = 0;
    
                    while (true) {
                        String quantityStr = JOptionPane.showInputDialog(quantityFrame,
                                "Enter quantity for " + itemName + " (Available: " + availableQuantity + "):");
                        if (quantityStr != null && !quantityStr.isEmpty()) {
                            try {
                                quantity = Integer.parseInt(quantityStr);
                                if (quantity > 0 && quantity <= availableQuantity) {
                                    selectedItems.append(itemName).append(" - Quantity: ").append(quantity).append("\n");
                                    break; // Valid input, break the loop
                                } else {
                                    JOptionPane.showMessageDialog(quantityFrame,
                                            "Quantity must be greater than zero and cannot exceed available quantity (" + availableQuantity + ").");
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(quantityFrame,
                                        "Invalid quantity entered. Please enter a valid number.");
                            }
                        } else {
                            break; // User canceled the input
                        }
                    }
                }
    
                if (selectedItems.length() == 0) {
                    JOptionPane.showMessageDialog(quantityFrame , "Please select at least one item.");
                } else {
                    try (Connection connection = DatabaseConnection.getConnection()) {
                        int reservationId = getReservationId(selectedEventType);
                        if (reservationId == -1) {
                            JOptionPane.showMessageDialog(quantityFrame,
                                    "Error: No valid reservation ID found for logging.");
                            return; // Exit if no valid reservation ID
                        }
    
                        for (String line : selectedItems.toString().split("\n")) {
                            String[] parts = line.split(" - ");
                            if (parts.length == 2) {
                                String itemName = parts[0].trim();
                                int quantity = Integer.parseInt(parts[1].replaceAll("[^0-9]", "").trim());
    
                                // Deduct from 'Inside' inventory
                                String deductQuery = "UPDATE Inventory SET quantity = quantity - ? WHERE item_name = ? AND item_type = 'Inside'";
                                PreparedStatement deductStatement = connection.prepareStatement(deductQuery);
                                deductStatement.setInt(1, quantity);
                                deductStatement.setString(2, itemName);
                                int rowsUpdated = deductStatement.executeUpdate();
    
                                if (rowsUpdated > 0) {
                                    // Log the action of bringing out items
                                    int itemId = getItemId(itemName);
                                    logItemAction(itemId, reservationId, quantity, "brought out");
    
                                    // Check if item already exists in 'Outside' inventory
                                    String checkQuery = "SELECT quantity FROM Inventory WHERE item_name = ? AND item_type = 'Outside'";
                                    PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                                    checkStatement.setString(1, itemName);
                                    ResultSet checkResultSet = checkStatement.executeQuery();
    
                                    if (checkResultSet.next()) {
                                        // Item exists, update quantity
                                        int existingQuantity = checkResultSet.getInt("quantity");
    
                                        String updateQuery = "UPDATE Inventory SET quantity = ? WHERE item_name = ? AND item_type = 'Outside'";
                                        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                                        updateStatement.setInt(1, existingQuantity + quantity);
                                        updateStatement.setString(2, itemName);
                                        updateStatement.executeUpdate();
                                    } else {
                                        // Item does not exist, insert new record
                                        String insertQuery = "INSERT INTO Inventory (item_name, quantity, item_type) VALUES (?, ?, 'Outside')";
                                        PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                                        insertStatement.setString(1, itemName);
                                        insertStatement.setInt(2, quantity);
                                        insertStatement.executeUpdate();
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(quantityFrame,
                                            "Not enough quantity in 'Inside' inventory for " + itemName);
                                }
                            }
                        }
                        JOptionPane.showMessageDialog(quantityFrame, "Items successfully brought out!");
                        quantityFrame.dispose(); // Close the quantity frame
                        inventoryFrame.dispose(); // Close the inventory frame
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(quantityFrame, "Error updating inventory: " + ex.getMessage());
                    }
                }
            });
            quantityFrame.add(enterQuantitiesButton);
            quantityFrame.setVisible(true);
        });
    
        inventoryFrame.add(confirmButton);
        inventoryFrame.setVisible(true);
    }
    
    
    

    private void returnItems() {
        JFrame eventFrame = new JFrame("Select Event");
        eventFrame.setLayout(null);
        eventFrame.setResizable(false);
    
        JLabel eventLabel = new JLabel("Select Event:");
        eventLabel.setBounds(50, 20, 200, 30);
        eventFrame.add(eventLabel);
    
        DefaultListModel<String> eventModel = new DefaultListModel<>();
        loadApprovedEvents(eventModel); // Load approved events when the frame is opened
    
        JList<String> eventList = new JList<>(eventModel);
        eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane eventScrollPane = new JScrollPane(eventList);
        eventScrollPane.setBounds(50, 60, 300, 200);
        eventFrame.add(eventScrollPane);
    
        JButton returnItemsButton = new JButton("Return Items");
        returnItemsButton.setBounds(210, 280, 150, 30);
        returnItemsButton.addActionListener(e -> {
            String selectedEvent = eventList.getSelectedValue();
            if (selectedEvent != null) {
                selectedEventType = selectedEvent; // Set the selected event type
                showEventDetails(selectedEvent);
            } else {
                JOptionPane.showMessageDialog(eventFrame, "Please select an event.");
            }
        });
        eventFrame.add(returnItemsButton);
    
        eventFrame.setSize(400, 350);
        eventFrame.setLocationRelativeTo(null);
        eventFrame.setVisible(true);
    }
    
    
    private void loadApprovedEvents(DefaultListModel<String> eventModel) {
        eventModel.clear(); // Clear existing events
        for (String eventType : usedEventTypes) { // Load only used events
            eventModel.addElement(eventType);
        }
    }
    private void returnItemInput(String eventType) {
        JFrame returnFrame = new JFrame("Return Item");
        returnFrame.setLayout(null);
        returnFrame.setResizable(false);

        JLabel itemLabel = new JLabel("Enter Item Name:");
        itemLabel.setBounds(50, 20, 200, 30);
        returnFrame.add(itemLabel);

        JTextField itemNameField = new JTextField();
        itemNameField.setBounds(50, 60, 300, 30);
        returnFrame.add(itemNameField);

        JLabel quantityLabel = new JLabel("Enter Quantity:");
        quantityLabel.setBounds(50, 100, 200, 30);
        returnFrame.add(quantityLabel);

        JTextField quantityField = new JTextField();
        quantityField.setBounds(50, 140, 300, 30);
        returnFrame.add(quantityField);

        JButton proceedButton = new JButton("Proceed");
        proceedButton.setBounds(150, 200, 100, 30);
        proceedButton.addActionListener(e -> {
            String itemName = itemNameField.getText().trim();
            String quantityStr = quantityField.getText().trim();
            if (itemName.isEmpty() || quantityStr.isEmpty()) {
                JOptionPane.showMessageDialog(returnFrame, "Please enter both item name and quantity.");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(returnFrame, "Invalid quantity. Please enter a positive number.");
                return;
            }

            // Logic to validate and log missing items
            int originalQuantity = getExistingQuantity(itemName);
            validateReturnItems(eventType, itemName, quantity, originalQuantity, null, null);
        });
        returnFrame.add(proceedButton);

        returnFrame.setSize(400, 300);
        returnFrame.setLocationRelativeTo(null);
        returnFrame.setVisible(true);
    }

    private void showEventDetails(String eventType) {
        JFrame detailsFrame = new JFrame("Event Details ");
        detailsFrame.setLayout(null);
        detailsFrame.setResizable(false);
        detailsFrame.setSize(600, 400); // Adjusted size
        
        DefaultListModel<String> itemModel = new DefaultListModel<>();
        DefaultListModel<Integer> quantityModel = new DefaultListModel<>(); // To store quantities
        try (Connection connection = DatabaseConnection.getConnection()) {
            String itemQuery = "SELECT i.item_name, SUM(il.quantity) AS total_quantity " +
                    "FROM item_logs il " +
                    "INNER JOIN Inventory i ON il.item_id = i.item_id " +
                    "INNER JOIN Reservations r ON il.reservation_id = r.reservation_id " +
                    "WHERE r.event_type = ? AND il.action = 'brought out' " +
                    "GROUP BY i.item_name";
            PreparedStatement itemStatement = connection.prepareStatement(itemQuery);
            itemStatement.setString(1, eventType);
            ResultSet itemResultSet = itemStatement.executeQuery();
        
            while (itemResultSet.next()) {
                String itemDetails = itemResultSet.getString("item_name") + " - Quantity: " +
                        itemResultSet.getInt("total_quantity");
                itemModel.addElement(itemDetails);
                quantityModel.addElement(itemResultSet.getInt("total_quantity")); // Store the quantity
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(detailsFrame, "Error fetching item details: " + e.getMessage());
            return;
        }
        
        JList<String> itemList = new JList<>(itemModel);
        JScrollPane itemScrollPane = new JScrollPane(itemList);
        itemScrollPane.setBounds(50, 20, 500, 250); // Adjusted bounds to fit new size
        detailsFrame.add(itemScrollPane);
        
        JButton returnButton = new JButton("Return Selected Items");
        returnButton.setBounds(100, 280, 200, 30);
        returnButton.addActionListener(e -> {
            for (int i = 0; i < itemList.getSelectedValuesList().size(); i++) {
                String selectedItem = itemList.getSelectedValuesList().get(i);
                String itemName = selectedItem.split(" - ")[0]; // Extract item name
                int originalQuantity = quantityModel.get(i); // Get the original quantity
                int quantity = 0;
    
                while (true) {
                    String quantityStr = JOptionPane.showInputDialog(detailsFrame, "Enter quantity for " + itemName + " (Available: " + originalQuantity + "):");
                    if (quantityStr != null && !quantityStr.isEmpty()) {
                        try {
                            quantity = Integer.parseInt(quantityStr);
                            if (quantity > 0 && quantity <= originalQuantity) {
                                validateReturnItems(eventType, itemName, quantity, originalQuantity, itemModel, quantityModel); // Call with all parameters
                                break; // Valid input, break the loop
                            } else {
                                JOptionPane.showMessageDialog(detailsFrame, "Quantity must be greater than zero and cannot exceed available quantity (" + originalQuantity + ").");
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(detailsFrame, "Invalid quantity entered. Please enter a valid number.");
                        }
                    } else {
                        break; // User canceled the input
                    }
                }
            }
        });
        detailsFrame.add(returnButton);
        
        JButton doneButton = new JButton("Done");
        doneButton.setBounds(320, 280, 200, 30);
        doneButton.addActionListener(e -> {
            // Logic to save the updated quantities (if any changes were made)
            for (int i = 0; i < itemModel.getSize(); i++) {
                String itemDetails = itemModel.get(i);
                String itemName = itemDetails.split(" - ")[0];
                int newQuantity = Integer.parseInt(itemDetails.split(": ")[1]); // Get the new quantity
    
                // Update the inventory with the new quantity
                try (Connection connection = DatabaseConnection.getConnection()) {
                    String updateQuery = "UPDATE Inventory SET quantity = ? WHERE item_name = ? AND item_type = 'Outside'";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setInt(1, newQuantity);
                    updateStatement.setString(2, itemName);
                    updateStatement.executeUpdate();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, "Error updating inventory: " + ex.getMessage());
                }
            }
    
            // Mark the event as completed
            try (Connection connection = DatabaseConnection.getConnection()) {
                String markCompletedQuery = "UPDATE Reservations SET status = 'Completed' WHERE event_type = ?";
                PreparedStatement markCompletedStatement = connection.prepareStatement(markCompletedQuery);
                markCompletedStatement.setString(1, selectedEventType);
                markCompletedStatement.executeUpdate();
                JOptionPane.showMessageDialog(detailsFrame, "Event marked as completed successfully.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(detailsFrame, "Error marking event as completed: " + ex.getMessage());
            }
    
            detailsFrame.dispose(); // Close the details frame and return to the previous frame
        });
        detailsFrame.add(doneButton);
    
        detailsFrame.setLocationRelativeTo(null);
        detailsFrame.setVisible(true);
    }

    private void validateReturnItems(String eventType, String itemName, int quantity, int originalQuantity,
            DefaultListModel<String> itemModel, DefaultListModel<Integer> quantityModel) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            String checkQuery = "SELECT SUM(il.quantity) AS total_quantity, i.price " +
                    "FROM item_logs il " +
                    "INNER JOIN Reservations r ON il.reservation_id = r.reservation_id " +
                    "INNER JOIN Inventory i ON il.item_id = i.item_id " +
                    "WHERE r.event_type = ? AND il.item_id = (SELECT item_id FROM Inventory WHERE item_name = ? LIMIT 1) "
                    +
                    "GROUP BY i.price";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, eventType);
            checkStatement.setString(2, itemName);
            ResultSet checkResultSet = checkStatement.executeQuery();

            int totalQuantityBroughtOut = 0;
            double itemPrice = 0;
            if (checkResultSet.next()) {
                totalQuantityBroughtOut = checkResultSet.getInt("total_quantity");
                itemPrice = checkResultSet.getDouble("price");
            }

            // Check if the quantity returned is less than what was brought out
            if (quantity < totalQuantityBroughtOut) {
                int missingQuantity = totalQuantityBroughtOut - quantity;
                double totalValue = missingQuantity * itemPrice;

                // Log missing items
                int itemId = getItemId(itemName);
                int reservationId = getReservationId(selectedEventType);
                logItemAction(itemId, reservationId, missingQuantity, "missing");

                JOptionPane.showMessageDialog(null, "Warning: You are missing " + missingQuantity + " of " + itemName
                        + " to return. (Value: Php" + totalValue + ")");
            }

            // Calculate remaining quantity needed to be returned
            int remainingQuantity = totalQuantityBroughtOut - quantity;

            // Proceed to update inventory
            updateInventoryAfterReturn(itemName, quantity, originalQuantity, itemModel, quantityModel,
                    totalQuantityBroughtOut, remainingQuantity);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error validating return items: " + e.getMessage());
        }
    }

    private void updateInventoryAfterReturn(String itemName, int quantity, int originalQuantity,
            DefaultListModel<String> itemModel, DefaultListModel<Integer> quantityModel, int totalQuantityBroughtOut,
            int remainingQuantity) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Get the item ID and reservation ID
            int itemId = getItemId(itemName);
            int reservationId = getReservationId(selectedEventType); // Ensure the reservation ID is fetched here

            // Check if itemId or reservationId is invalid
            if (itemId == -1 || reservationId == -1) {
                JOptionPane.showMessageDialog(null, "Error: Invalid item ID (" + itemId + ") or reservation ID ("
                        + reservationId + ") for logging.");
                return; // Exit if either ID is invalid
            }

            // Update inventory quantity for 'Outside'
            String updateOutsideQuery = "UPDATE Inventory SET quantity = quantity - ? WHERE item_name = ? AND item_type = 'Outside'";
            PreparedStatement updateOutsideStatement = connection.prepareStatement(updateOutsideQuery);
            updateOutsideStatement.setInt(1, quantity);
            updateOutsideStatement.setString(2, itemName);
            updateOutsideStatement.executeUpdate();

            // Update inside inventory
            String updateInsideQuery = "UPDATE Inventory SET quantity = quantity + ? WHERE item_name = ? AND item_type = 'Inside'";
            PreparedStatement updateInsideStatement = connection.prepareStatement(updateInsideQuery);
            updateInsideStatement.setInt(1, quantity);
            updateInsideStatement.setString(2, itemName);
            updateInsideStatement.executeUpdate();

            // Log item action
            logItemAction(itemId, reservationId, quantity, "returned ");

            // Update the item list based on remaining quantities
            if (remainingQuantity > 0) {
                // Update the item model to reflect the remaining quantity
                itemModel.removeElement(itemName + " - Quantity: " + totalQuantityBroughtOut);
                itemModel.addElement(itemName + " - Quantity: " + remainingQuantity);
                quantityModel.removeElement(totalQuantityBroughtOut);
                quantityModel.addElement(remainingQuantity);
            } else {
                // Mark item as done if all quantities have been returned
                itemModel.removeElement(itemName + " - Quantity: " + totalQuantityBroughtOut);
                quantityModel.removeElement(totalQuantityBroughtOut);
                JOptionPane.showMessageDialog(null,
                        itemName + " has been marked as done since all quantities have been returned.");
            }

            JOptionPane.showMessageDialog(null, "Items successfully returned!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error updating inventory: " + ex.getMessage());
        }
    }


    private int getItemId(String itemName) {
        int itemId = -1; // Default to -1 to indicate not found
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT item_id FROM Inventory WHERE item_name = ? AND item_type = 'Inside'";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, itemName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                itemId = resultSet.getInt("item_id"); // Get the item ID
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error retrieving item ID: " + e.getMessage());
        }
        return itemId; // Return the found item ID or -1 if not found
    }

    private int getExistingQuantity(String itemName) {
        int existingQuantity = 0;
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT quantity FROM Inventory WHERE item_name = ? AND item_type = 'Outside'";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, itemName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                existingQuantity = resultSet.getInt("quantity"); // Get the existing quantity
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error retrieving existing quantity: " + e.getMessage());
        }
        return existingQuantity;
    }

    private void logItemAction(int itemId, Integer reservationId, int quantity, String action) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Ensure the action string is within an acceptable length
            if (action.length() > 30) {
                action = action.substring(0, 30); // Truncate if necessary
            }

            // Only log if the itemId is valid and reservationId exists
            if (itemId > 0 && reservationId != null) {
                String logQuery = "INSERT INTO item_logs (item_id, reservation_id, action, quantity) VALUES (?, ?, ?, ?)";
                PreparedStatement logStatement = connection.prepareStatement(logQuery);
                logStatement.setInt(1, itemId);
                logStatement.setInt(2, reservationId);
                logStatement.setString(3, action);
                logStatement.setInt(4, quantity);
                logStatement.executeUpdate();
            } else if (quantity < 0) { // Log missing items
                String logQuery = "INSERT INTO item_logs (item_id, action, quantity) VALUES (?, ?, ?)";
                PreparedStatement logStatement = connection.prepareStatement(logQuery);
                logStatement.setInt(1, itemId);
                logStatement.setString(2, "missing");
                logStatement.setInt(3, -quantity); // Store missing quantity as positive
                logStatement.executeUpdate();
            } else {
                JOptionPane.showMessageDialog(null, "Error: Invalid item ID or reservation ID for logging.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error logging item action: " + e.getMessage());
        }
    }

    private int getReservationId(String eventType) {
        int reservationId = -1; // Default to -1 to indicate not found
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT reservation_id FROM Reservations WHERE event_type = ? AND status = 'Approved' LIMIT 1"; // Limit
                                                                                                                           // to
                                                                                                                           // 1
                                                                                                                           // result
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, eventType);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                reservationId = resultSet.getInt("reservation_id"); // Get the reservation ID
            } else {
                JOptionPane.showMessageDialog(null, "No approved reservation found for the selected event type.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error retrieving reservation ID: " + e.getMessage());
        }
        return reservationId; // Return the found reservation ID or -1 if not found
    }

    private void updateInventoryForMissingItems(String itemName, int missingQuantity) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Update the inventory for missing items
            String updateOutsideQuery = "UPDATE Inventory SET quantity = quantity - ? WHERE item_name = ? AND item_type = 'Outside'";
            PreparedStatement updateOutsideStatement = connection.prepareStatement(updateOutsideQuery);
            updateOutsideStatement.setInt(1, missingQuantity);
            updateOutsideStatement.setString(2, itemName);
            updateOutsideStatement.executeUpdate();

            // Log the action of returning missing items
            int itemId = getItemId(itemName);
            int reservationId = getReservationId(selectedEventType);
            logItemAction(itemId, reservationId, missingQuantity, "returned missing");

            JOptionPane.showMessageDialog(null, "Missing items successfully returned!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error updating inventory for missing items: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StaffDashboard::new);
    }
}