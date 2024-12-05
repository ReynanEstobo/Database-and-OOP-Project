import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(600, 500);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JLabel welcomeLabel = new JLabel("Welcome to the Admin Dashboard", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setBounds(0, 30, 600, 40);
        add(welcomeLabel);

        int buttonWidth = 200;
        int buttonHeight = 40;
        int buttonYSpacing = 60; // Space between buttons
        int startY = 100; // Starting Y position for buttons

        // Center the buttons
        int startX = (getWidth() - buttonWidth) / 2;

        JButton viewInventoryButton = createButton("View Inventory", startX, startY, buttonWidth, buttonHeight);
        viewInventoryButton.addActionListener(e -> viewInventory());
        add(viewInventoryButton);

        JButton viewBookRequestButton = createButton("View Book Requests", startX, startY + buttonYSpacing, buttonWidth,
                buttonHeight);
        viewBookRequestButton.addActionListener(e -> viewBookRequests());
        add(viewBookRequestButton);

        JButton viewLogButton = createButton("View Log of Items", startX, startY + 2 * buttonYSpacing, buttonWidth,
                buttonHeight);
        viewLogButton.addActionListener(e -> viewLogOfItems());
        add(viewLogButton);

        JButton alterReservationButton = createButton("Alter Reservations", startX, startY + 3 * buttonYSpacing,
                buttonWidth, buttonHeight);
        alterReservationButton.addActionListener(e -> alterReservations());
        add(alterReservationButton);

        JButton logoutButton = createButton("Log Out", startX, startY + 4 * buttonYSpacing, buttonWidth, buttonHeight);
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

    private void viewInventory() {
        JFrame inventoryFrame = new JFrame("Inventory");
        inventoryFrame.setSize(800, 650);
        inventoryFrame.setLayout(null);
        inventoryFrame.setResizable(false);
        inventoryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        // Table setup for Inside Items
        String[] columnNames = { "Item ID", "Item Name", "Item Type", "Stock", "Price (per each)" };
        DefaultTableModel insideModel = new DefaultTableModel(columnNames, 0);
        JTable insideInventoryTable = new JTable(insideModel);
        JScrollPane insideScrollPane = new JScrollPane(insideInventoryTable);
        insideScrollPane.setBounds(30, 60, 720, 200);
        inventoryFrame.add(insideScrollPane);
    
        // Label for Inside Items
        JLabel insideLabel = new JLabel("Inside Items");
        insideLabel.setBounds(30, 30, 200, 20);
        insideLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        inventoryFrame.add(insideLabel);
    
        // Table setup for Outside Items
        DefaultTableModel outsideModel = new DefaultTableModel(columnNames, 0);
        JTable outsideInventoryTable = new JTable(outsideModel);
        JScrollPane outsideScrollPane = new JScrollPane(outsideInventoryTable);
        outsideScrollPane.setBounds(30, 320, 720, 200);
        inventoryFrame.add(outsideScrollPane);
    
        // Label for Outside Items
        JLabel outsideLabel = new JLabel("Outside Items");
        outsideLabel.setBounds(30, 290, 200, 20);
        outsideLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        inventoryFrame.add(outsideLabel);
    
        // Button setup with adjusted positions
        int buttonWidth = 120;
        int buttonHeight = 30;
        int buttonYPosition = 540; // Y position for all buttons
        int buttonSpacing = 20; // Space between buttons
    
        JButton addButton = createButton("Add Item", 40, buttonYPosition, buttonWidth, buttonHeight);
        addButton.addActionListener(e -> addItem());
        inventoryFrame.add(addButton);
    
        JButton editButton = createButton("Edit Item", 40 + buttonWidth + buttonSpacing, buttonYPosition, buttonWidth,
                buttonHeight);
        editButton.addActionListener(e -> editItem(insideInventoryTable)); // Edit for Inside Items only
        inventoryFrame.add(editButton);
    
        JButton removeButton = createButton("Remove Item", 40 + 2 * (buttonWidth + buttonSpacing), buttonYPosition,
                buttonWidth, buttonHeight);
        removeButton.addActionListener(e -> removeItem(insideInventoryTable)); // Remove for Inside Items only
        inventoryFrame.add(removeButton);
    
        JButton refreshButton = createButton("Refresh", 40 + 3 * (buttonWidth + buttonSpacing), buttonYPosition,
                buttonWidth, buttonHeight);
        refreshButton.addActionListener(e -> loadInventory(insideModel, outsideModel));
        inventoryFrame.add(refreshButton);
    
        loadInventory(insideModel, outsideModel); // Load inventory items from the database when the frame is opened.
    
        inventoryFrame.setLocationRelativeTo(null);
        inventoryFrame.setVisible(true);
    }
    
    private void loadInventory(DefaultTableModel insideModel, DefaultTableModel outsideModel) {
        insideModel.setRowCount(0); // Clear existing rows for Inside Items
        outsideModel.setRowCount(0); // Clear existing rows for Outside Items
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Load Inside Items
            String insideQuery = "SELECT item_id, item_name, item_type, quantity, price FROM inventory WHERE item_type = 'Inside'";
            PreparedStatement insideStatement = connection.prepareStatement(insideQuery);
            ResultSet insideResultSet = insideStatement.executeQuery();
    
            while (insideResultSet.next()) {
                int itemId = insideResultSet.getInt("item_id");
                String itemName = insideResultSet.getString("item_name");
                String itemType = insideResultSet.getString("item_type");
                int quantity = insideResultSet.getInt("quantity");
                double price = insideResultSet.getDouble("price");
                insideModel.addRow(new Object[]{itemId, itemName, itemType, quantity, price}); // Add to Inside Items table
            }
    
            // Load Outside Items
            String outsideQuery = "SELECT item_id, item_name, item_type, quantity, price FROM inventory WHERE item_type = 'Outside'";
            PreparedStatement outsideStatement = connection.prepareStatement(outsideQuery);
            ResultSet outsideResultSet = outsideStatement.executeQuery();
    
            while (outsideResultSet.next()) {
                int itemId = outsideResultSet.getInt("item_id");
                String itemName = outsideResultSet.getString("item_name");
                String itemType = outsideResultSet.getString("item_type");
                int quantity = outsideResultSet.getInt("quantity");
                double price = outsideResultSet.getDouble("price");
                outsideModel.addRow(new Object[]{itemId, itemName, itemType, quantity, price}); // Add to Outside Items table
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading inventory: " + e.getMessage());
        }
    }

    private void loadInventory(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT item_id, item_name, item_type, quantity, price FROM inventory"; // Include price in
                                                                                                   // the query
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int itemId = resultSet.getInt("item_id");
                String itemName = resultSet.getString("item_name");
                String itemType = resultSet.getString("item_type");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price"); // Retrieve price

                // Add the item directly to the model
                model.addRow(new Object[] { itemId, itemName, itemType, quantity, price }); // Add price to the model
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading inventory: " + e.getMessage());
        }
    }

    private void addItem() {
        JFrame addItemFrame = new JFrame("Add Item");
        addItemFrame.setSize(400, 350); // Adjusted size to accommodate price field
        addItemFrame.setLayout(null);
        addItemFrame.setResizable(false);
    
        JLabel nameLabel = new JLabel("Item Name:");
        nameLabel.setBounds(50, 50, 100, 30);
        JTextField nameField = new JTextField();
        nameField.setBounds(150, 50, 200, 30);
    
        JLabel typeLabel = new JLabel("Item Type:");
        typeLabel.setBounds(50, 100, 100, 30);
        JTextField typeField = new JTextField();
        typeField.setBounds(150, 100, 200, 30);
    
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(50, 150, 100, 30);
        JTextField quantityField = new JTextField();
        quantityField.setBounds(150, 150, 200, 30);
    
        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setBounds(50, 200, 100, 30);
        JTextField priceField = new JTextField();
        priceField.setBounds(150, 200, 200, 30);
    
        JButton confirmButton = new JButton("Add Item");
        confirmButton.setBounds(150, 250, 100, 30);
        confirmButton.addActionListener(e -> {
            String itemName = nameField.getText();
            String itemType = typeField.getText();
            String quantity = quantityField.getText();
            String price = priceField.getText();
    
            if (!itemName.isEmpty() && !itemType.isEmpty() && !quantity.isEmpty() && !price.isEmpty()) {
                if (addItemToDatabase(itemName, itemType, Integer.parseInt(quantity), Double.parseDouble(price))) {
                    JOptionPane.showMessageDialog(addItemFrame, "Item added successfully!");
                    addItemFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(addItemFrame, "Error adding item. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(addItemFrame, "Please fill in all fields.");
            }
        });
    
        addItemFrame.add(nameLabel);
        addItemFrame.add(nameField);
        addItemFrame.add(typeLabel);
        addItemFrame.add(typeField);
        addItemFrame.add(quantityLabel);
        addItemFrame.add(quantityField);
        addItemFrame.add(priceLabel);
        addItemFrame.add(priceField);
        addItemFrame.add(confirmButton);
    
        addItemFrame.setLocationRelativeTo(null);
        addItemFrame.setVisible(true);
    }

    private boolean addItemToDatabase(String itemName, String itemType, int quantity, double price) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Check if the item already exists based on both name and type
            String checkQuery = "SELECT item_id, quantity FROM inventory WHERE item_name = ? AND item_type = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, itemName);
            checkStatement.setString(2, itemType);
            ResultSet checkResultSet = checkStatement.executeQuery();
    
            if (checkResultSet.next()) {
                // Item exists, update the quantity
                int existingItemId = checkResultSet.getInt("item_id");
                int existingQuantity = checkResultSet.getInt("quantity");
                int newQuantity = existingQuantity + quantity;
    
                String updateQuery = "UPDATE inventory SET quantity = ?, price = ? WHERE item_id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setInt(1, newQuantity);
                updateStatement.setDouble(2, price); // Update price as well
                updateStatement.setInt(3, existingItemId);
                updateStatement.executeUpdate();
    
                return true; // Quantity updated successfully
            } else {
                // Item does not exist, insert new item
                String insertQuery = "INSERT INTO inventory (item_name, item_type, quantity, price) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, itemName);
                insertStatement.setString(2, itemType);
                insertStatement.setInt(3, quantity);
                insertStatement.setDouble(4, price); // Insert price as well
                int rowsInserted = insertStatement.executeUpdate();
                return rowsInserted > 0; // Item added successfully
            }
        } catch (SQLException e) {
     e.printStackTrace();
            return false;
        }
    }

    private void editItem(JTable inventoryTable) {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            int itemId = (int) inventoryTable.getValueAt(selectedRow, 0);
            String itemName = (String) inventoryTable.getValueAt(selectedRow, 1);
            String itemType = (String) inventoryTable.getValueAt(selectedRow, 2);
            int quantity = (int) inventoryTable.getValueAt(selectedRow, 3);
            double price = (double) inventoryTable.getValueAt(selectedRow, 4); // Get current price
    
            JFrame editItemFrame = new JFrame("Edit Item");
            editItemFrame.setSize(400, 350); // Adjusted size to accommodate price field
            editItemFrame.setLayout(null);
            editItemFrame.setResizable(false);
    
            JLabel nameLabel = new JLabel("Item Name:");
            nameLabel.setBounds(50, 50, 100, 30);
            JTextField nameField = new JTextField(itemName);
            nameField.setBounds(150, 50, 200, 30);
    
            JLabel typeLabel = new JLabel("Item Type:");
            typeLabel.setBounds(50, 100, 100, 30);
            JTextField typeField = new JTextField(itemType);
            typeField.setBounds(150, 100, 200, 30);
    
            JLabel quantityLabel = new JLabel("Quantity:");
            quantityLabel.setBounds(50, 150, 100, 30);
            JTextField quantityField = new JTextField(String.valueOf(quantity));
            quantityField.setBounds(150, 150, 200, 30);
    
            JLabel priceLabel = new JLabel("Price:");
            priceLabel.setBounds(50, 200, 100, 30);
            JTextField priceField = new JTextField(String.valueOf(price));
            priceField.setBounds(150, 200, 200, 30);
    
            JButton confirmButton = new JButton("Update Item");
            confirmButton.setBounds(150, 250, 100, 30);
            confirmButton.addActionListener(e -> {
                String newItemName = nameField.getText();
                String newItemType = typeField.getText();
                String newQuantity = quantityField.getText();
                String newPrice = priceField.getText();
    
                if (!newItemName.isEmpty() && !newItemType.isEmpty() && !newQuantity.isEmpty() && !newPrice.isEmpty()) {
                    if (updateItemInDatabase(itemId, newItemName, newItemType, Integer.parseInt(newQuantity), Double.parseDouble(newPrice))) {
                        JOptionPane.showMessageDialog(editItemFrame, "Item updated successfully!");
                        editItemFrame.dispose();
                        loadInventory((DefaultTableModel) inventoryTable.getModel()); // Refresh the inventory
                    } else {
                        JOptionPane.showMessageDialog(editItemFrame, "Error updating item. Please try again.");
                    }
                } else {
                    JOptionPane.showMessageDialog(editItemFrame, "Please fill in all fields.");
                }
            });
    
            editItemFrame.add(nameLabel);
            editItemFrame.add(nameField);
            editItemFrame.add(typeLabel);
            editItemFrame.add(typeField);
            editItemFrame.add(quantityLabel);
            editItemFrame.add(quantityField);
            editItemFrame.add(priceLabel);
            editItemFrame.add(priceField);
            editItemFrame.add(confirmButton);
    
            editItemFrame.setLocationRelativeTo(null);
            editItemFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Please select an item to edit.");
        }
    }

    private boolean updateItemInDatabase(int itemId, String itemName, String itemType, int quantity, double price) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE inventory SET item_name = ?, item_type = ?, quantity = ?, price = ? WHERE item_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, itemName);
            statement.setString(2, itemType);
            statement.setInt(3, quantity);
            statement.setDouble(4, price); // Update price
            statement.setInt(5, itemId);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void removeItem(JTable inventoryTable) {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            int itemId = (int) inventoryTable.getValueAt(selectedRow, 0);
            int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this item?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                if (removeItemFromDatabase(itemId)) {
                    JOptionPane.showMessageDialog(null, "Item removed successfully!");
                    ((DefaultTableModel) inventoryTable.getModel()).removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(null, "Error removing item. Please try again.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select an item to remove.");
        }
    }

    private boolean removeItemFromDatabase(int itemId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM inventory WHERE item_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, itemId);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void viewBookRequests() {
        JFrame bookRequestsFrame = new JFrame("Book Requests");
        bookRequestsFrame.setSize(1000, 600);

        bookRequestsFrame.setLayout(null);
        bookRequestsFrame.setResizable(false);

        String[] columnNames = { "Reservation ID", "Client ID", "Date", "Time", "Event Type", "Guest Count", "Place",
                "Status" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable requestsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(requestsTable);

        // Set the preferred size for the scroll pane
        scrollPane.setPreferredSize(new Dimension(940, 400));
        scrollPane.setBounds(30, 30, 940, 400);
        bookRequestsFrame.add(scrollPane);

        JButton approveButton = createButton("Approve", 150, 460, 120, 30);
        approveButton.addActionListener(e -> approveReservation(requestsTable));
        bookRequestsFrame.add(approveButton);

        JButton rejectButton = createButton("Reject", 350, 460, 120, 30);
        rejectButton.addActionListener(e -> rejectReservation(requestsTable));
        bookRequestsFrame.add(rejectButton);

        // Create the Back button
        JButton backButton = createButton("Back", 550, 460, 120, 30);
        backButton.addActionListener(e -> {
            bookRequestsFrame.dispose(); // Close the current frame
            new AdminDashboard(); // Open the AdminDashboard again
        });
        bookRequestsFrame.add(backButton);

        loadBookRequests(model); // Load book requests from the database when the frame is opened.

        bookRequestsFrame.setLocationRelativeTo(null);
        bookRequestsFrame.setVisible(true);
    }

    private void loadBookRequests(DefaultTableModel model) {
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
                String guestCount = resultSet.getString("guest_count");
                String place = resultSet.getString("place");
                String status = resultSet.getString("status");
                model.addRow(
                        new Object[] { reservationId, clientId, date, time, eventType, guestCount, place, status });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading book requests.");
        }
    }

    private void approveReservation(JTable requestsTable) {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reservationId = (int) requestsTable.getValueAt(selectedRow, 0);
            updateReservationStatus(reservationId, "Approved", null);
            JOptionPane.showMessageDialog(null, "Reservation approved successfully.");
            ((DefaultTableModel) requestsTable.getModel()).removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(null, "Please select a reservation to approve.");
        }
    }

    private void rejectReservation(JTable requestsTable) {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String reason = JOptionPane.showInputDialog("Please enter the reason for rejection:");
            if (reason != null && !reason.trim().isEmpty()) {
                int reservationId = (int) requestsTable.getValueAt(selectedRow, 0);
                updateReservationStatus(reservationId, "Rejected", reason);
                JOptionPane.showMessageDialog(null, "Reservation rejected successfully.");
                ((DefaultTableModel) requestsTable.getModel()).removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(null, "Rejection reason cannot be empty.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a reservation to reject.");
        }
    }

    private void updateReservationStatus(int reservationId, String status, String reason) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE Reservations SET status = ?, rejection_reason = ? WHERE reservation_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, status);
            statement.setString(2, reason);
            statement.setInt(3, reservationId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating reservation status.");
        }
    }

    private void viewLogOfItems() {
        JFrame logFrame = new JFrame("Item Log");
        logFrame.setSize(1300, 800);
        logFrame.setLayout(null);
        logFrame.setResizable(false);
    
        // Create table models for each log type
        String[] columnNamesBroughtOut = { "Log ID", "Event Name", "Item Name", "Quantity", "Date" }; // Added Log ID
        String[] columnNamesReturned = { "Log ID", "Event Name", "Item Name", "Quantity", "Total Value", "Date" }; // Added Log ID
        String[] columnNamesMissing = { "Log ID", "Event Name", "Item Name", "Missing Quantity", "Total Value", "Date" }; // Added Log ID
    
        DefaultTableModel modelBroughtOut = new DefaultTableModel(columnNamesBroughtOut, 0);
        DefaultTableModel modelReturned = new DefaultTableModel(columnNamesReturned, 0);
        DefaultTableModel modelMissing = new DefaultTableModel(columnNamesMissing, 0);
    
        JTable tableBroughtOut = new JTable(modelBroughtOut);
        JTable tableReturned = new JTable(modelReturned);
        JTable tableMissing = new JTable(modelMissing);
    
        // Set selection mode to multiple selection
        tableBroughtOut.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableReturned.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableMissing.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    
        JScrollPane scrollPaneBroughtOut = new JScrollPane(tableBroughtOut);
        JScrollPane scrollPaneReturned = new JScrollPane(tableReturned);
        JScrollPane scrollPaneMissing = new JScrollPane(tableMissing);
    
        // Centering the scroll panes in the frame
        int scrollPaneWidth = 820;
        int scrollPaneHeight = 150;
        int startX = (1300 - scrollPaneWidth) / 2; // Centering X position
    
        scrollPaneBroughtOut.setBounds(startX, 70, scrollPaneWidth, scrollPaneHeight);
        scrollPaneReturned.setBounds(startX, 250, scrollPaneWidth, scrollPaneHeight);
        scrollPaneMissing.setBounds(startX, 430, scrollPaneWidth, scrollPaneHeight);
    
        logFrame.add(scrollPaneBroughtOut);
        logFrame.add(scrollPaneReturned);
        logFrame.add(scrollPaneMissing);
    
        // Centering the labels
        JLabel labelBroughtOut = new JLabel("Brought Out Logs");
        labelBroughtOut.setBounds(startX, 50, 200, 20);
        logFrame.add(labelBroughtOut);
    
        JLabel labelReturned = new JLabel("Returned Logs");
        labelReturned.setBounds(startX, 230, 200, 20);
        logFrame.add(labelReturned);
    
        JLabel labelMissing = new JLabel("Missing Items Log");
        labelMissing.setBounds(startX, 410, 200, 20);
        logFrame.add(labelMissing);
    
        // Adjusting the delete button position
        JButton deleteButton = createButton("Delete Selected", startX, 600, 150, 30);
        deleteButton.setToolTipText("Delete selected rows from the logs");
        deleteButton.addActionListener(e -> {
            // Remove selected rows from each table and delete from the database
            removeSelectedRows(tableBroughtOut, modelBroughtOut);
            removeSelectedRows(tableReturned, modelReturned);
            removeSelectedRows(tableMissing, modelMissing);
        });
        logFrame.add(deleteButton);
    
        // Adjusting the back button position
        JButton backButton = createButton("Back", startX + 700, 600, 120, 30);
        backButton.setToolTipText("Return to the previous screen");
        backButton.addActionListener(e -> {
            logFrame.dispose(); // Close the current frame
            new AdminDashboard(); // Open the AdminDashboard again
        });
        logFrame.add(backButton);
    
        loadLogEntries(modelBroughtOut, modelReturned, modelMissing); // Load log entries from the database
    
        logFrame.setLocationRelativeTo(null);
        logFrame.setVisible(true);
    }

    private void removeSelectedRows(JTable table, DefaultTableModel model) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 0) {
            int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the selected logs?", 
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            
            if (response == JOptionPane.YES_OPTION) {
                try (Connection connection = DatabaseConnection.getConnection()) {
                    // Prepare a delete statement
                    String deleteQuery = "DELETE FROM item_logs WHERE log_id = ?"; // Assuming log_id is the primary key
                    PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                    
                    // Iterate through selected rows in reverse order
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        int row = selectedRows[i];
                        int logId = (int) model.getValueAt(row, 0); // Assuming log_id is in the first column
    
                        deleteStatement.setInt(1, logId);
                        deleteStatement.executeUpdate(); // Execute delete for each selected log
    
                        model.removeRow(row); // Remove from the table model
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Error deleting log entries: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(table.getParent(), "Please select at least one row to delete.");
        }
    }

    private void loadLogEntries(DefaultTableModel modelBroughtOut, DefaultTableModel modelReturned,
        DefaultTableModel modelMissing) {
    modelBroughtOut.setRowCount(0); // Clear existing rows
    modelReturned.setRowCount(0);
    modelMissing.setRowCount(0);

    try (Connection connection = DatabaseConnection.getConnection()) {
        // Load brought out items
        String queryBroughtOut = "SELECT il.log_id, r.event_type, i.item_name, il.quantity, il.log_date " +
                "FROM item_logs il " +
                "INNER JOIN Inventory i ON il.item_id = i.item_id " +
                "INNER JOIN Reservations r ON il.reservation_id = r.reservation_id " +
                "WHERE il.action = 'brought out'";
        PreparedStatement statementBroughtOut = connection.prepareStatement(queryBroughtOut);
        ResultSet resultSetBroughtOut = statementBroughtOut.executeQuery();

        while (resultSetBroughtOut.next()) {
            int logId = resultSetBroughtOut.getInt("log_id"); // Get log ID
            String eventName = resultSetBroughtOut.getString("event_type");
            String itemName = resultSetBroughtOut.getString("item_name");
            int quantity = resultSetBroughtOut.getInt("quantity");
            String logDate = resultSetBroughtOut.getString("log_date");
            modelBroughtOut.addRow(new Object[] { logId, eventName, itemName, quantity, logDate }); // Include log ID
        }

        // Load returned items
        String queryReturned = "SELECT il.log_id, r.event_type, i.item_name, il.quantity, i.price, il.log_date " +
                "FROM item_logs il " +
                "INNER JOIN Inventory i ON il.item_id = i.item_id " +
                "INNER JOIN Reservations r ON il.reservation_id = r.reservation_id " +
                "WHERE il.action = 'returned'";
        PreparedStatement statementReturned = connection.prepareStatement(queryReturned);
        ResultSet resultSetReturned = statementReturned.executeQuery();

        while (resultSetReturned.next()) {
            int logId = resultSetReturned.getInt("log_id"); // Get log ID
            String eventName = resultSetReturned.getString("event_type");
            String itemName = resultSetReturned.getString("item_name");
            int quantity = resultSetReturned.getInt("quantity");
            double totalValue = quantity * resultSetReturned.getDouble("price");
            String logDate = resultSetReturned.getString("log_date");
            modelReturned.addRow(new Object[] { logId, eventName, itemName, quantity, totalValue, logDate }); // Include log ID
        }

        // Load missing items
        String queryMissing = "SELECT il.log_id, r.event_type, i.item_name, il.quantity, i.price, il.log_date " +
                "FROM item_logs il " +
                "INNER JOIN Inventory i ON il.item_id = i.item_id " +
                "INNER JOIN Reservations r ON il.reservation_id = r.reservation_id " +
                "WHERE il.action = 'missing'";
        PreparedStatement statementMissing = connection.prepareStatement(queryMissing);
        ResultSet resultSetMissing = statementMissing.executeQuery();

        while (resultSetMissing.next()) {
            int logId = resultSetMissing.getInt("log_id"); // Get log ID
            String eventName = resultSetMissing.getString("event_type");
            String itemName = resultSetMissing.getString("item_name");
            int missingQuantity = resultSetMissing.getInt("quantity");
            double totalValue = missingQuantity * resultSetMissing.getDouble("price");
            String logDate = resultSetMissing.getString("log_date");
            modelMissing.addRow(new Object[] { logId, eventName, itemName, missingQuantity, totalValue, logDate }); // Include log ID
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error loading log entries: " + e.getMessage());
    }
}

private void alterReservations() {
    JFrame reservationsFrame = new JFrame("Alter Reservations");
    reservationsFrame.setSize(800, 800); // Adjusted height to 800
    reservationsFrame.setLayout(null);
    reservationsFrame.setResizable(false);

    // Create table models for approved, rejected, and completed reservations
    String[] columnNames = { "Reservation ID", "Client ID", "Date", "Time", "Event Type", "Guest Count", "Place", "Status" };
    DefaultTableModel approvedModel = new DefaultTableModel(columnNames, 0);
    DefaultTableModel rejectedModel = new DefaultTableModel(columnNames, 0);
    DefaultTableModel completedModel = new DefaultTableModel(columnNames, 0); // New model for completed reservations

    // Create tables
    JTable approvedTable = new JTable(approvedModel);
    JTable rejectedTable = new JTable(rejectedModel);
    JTable completedTable = new JTable(completedModel); // New table for completed reservations

    JScrollPane approvedScrollPane = new JScrollPane(approvedTable);
    JScrollPane rejectedScrollPane = new JScrollPane(rejectedTable);
    JScrollPane completedScrollPane = new JScrollPane(completedTable); // New scroll pane for completed reservations
    approvedScrollPane.setBounds(30, 80, 720, 150);
    rejectedScrollPane.setBounds(30, 260, 720, 150);
    completedScrollPane.setBounds(30, 440, 720, 150); // Position for completed reservations

    reservationsFrame.add(approvedScrollPane);
    reservationsFrame.add(rejectedScrollPane);
    reservationsFrame.add(completedScrollPane); // Add completed reservations scroll pane

    // Add labels for each section
    JLabel approvedLabel = new JLabel("Approved Events");
    approvedLabel.setBounds(30, 50, 200, 20);
    approvedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    reservationsFrame.add(approvedLabel);

    JLabel rejectedLabel = new JLabel("Rejected Events");
    rejectedLabel.setBounds(30, 230, 200, 20);
    rejectedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    reservationsFrame.add(rejectedLabel);

    JLabel completedLabel = new JLabel("Completed Events");
    completedLabel.setBounds(30, 410, 200, 20);
    completedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    reservationsFrame.add(completedLabel);

    // Load reservations into the tables
    loadReservations(approvedModel, rejectedModel, completedModel); // Update method to load completed reservations

    // Button to edit selected reservation
    JButton editButton = createButton("Edit Reservation", 30, 620, 200, 30); // Adjusted position for the button
    editButton.addActionListener(e -> {
        int selectedRow = approvedTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reservationId = (int) approvedTable.getValueAt(selectedRow, 0);
            editReservation(reservationId);
        } else {
            JOptionPane.showMessageDialog(reservationsFrame, "Please select a reservation to edit(Approved, Rejected).");
        }
    });
    reservationsFrame.add(editButton);

    // Add a button to close the reservations frame
    JButton closeButton = createButton("Close", 30, 670, 200, 30); // New close button
    closeButton.addActionListener(e -> reservationsFrame.dispose());
    reservationsFrame.add(closeButton);

    reservationsFrame.setLocationRelativeTo(null);
    reservationsFrame.setVisible(true);
}



private void loadReservations(DefaultTableModel approvedModel, DefaultTableModel rejectedModel, DefaultTableModel completedModel) {
    approvedModel.setRowCount(0);
    rejectedModel.setRowCount(0);
    completedModel.setRowCount(0); // Clear completed model

    try (Connection connection = DatabaseConnection.getConnection()) {
        String query = "SELECT * FROM Reservations"; // Adjust the query as needed
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int reservationId = resultSet.getInt("reservation_id");
            int clientId = resultSet.getInt("client_id");
            String date = resultSet.getString("reservation_date");
            String time = resultSet.getString("reservation_time");
            String eventType = resultSet.getString("event_type");
            int guestCount = resultSet.getInt("guest_count");
            String place = resultSet.getString("place");
            String status = resultSet.getString("status");

            Object[] rowData = { reservationId, clientId, date, time, eventType, guestCount, place, status };
            if ("Approved".equals(status)) {
                approvedModel.addRow(rowData);
            } else if ("Rejected".equals(status)) {
                rejectedModel.addRow(rowData);
            } else if ("Completed".equals(status)) { // Check for completed status
                completedModel.addRow(rowData); // Add to completed model
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error loading reservations: " + e.getMessage());
    }
}

    private void editReservation(int reservationId) {
        JFrame editFrame = new JFrame("Edit Reservation");
        editFrame.setSize(400, 400);
        editFrame.setLayout(null);
        editFrame.setResizable(false);

        // Fetch existing reservation details
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Reservations WHERE reservation_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, reservationId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                JTextField clientIdField = new JTextField(String.valueOf(resultSet.getInt("client_id")));
                JTextField dateField = new JTextField(resultSet.getString("reservation_date"));
                JTextField timeField = new JTextField(resultSet.getString("reservation_time"));

                JTextField eventTypeField = new JTextField(resultSet.getString("event_type"));
                JTextField guestCountField = new JTextField(String.valueOf(resultSet.getInt("guest_count")));
                JTextField placeField = new JTextField(resultSet.getString("place"));

                clientIdField.setBounds(150, 30, 200, 30);
                dateField.setBounds(150, 70, 200, 30);
                timeField.setBounds(150, 110, 200, 30);
                eventTypeField.setBounds(150, 150, 200, 30);
                guestCountField.setBounds(150, 190, 200, 30);
                placeField.setBounds(150, 230, 200, 30);

                editFrame.add(new JLabel("Client ID:")).setBounds(30, 30, 100, 30);
                editFrame.add(clientIdField);
                editFrame.add(new JLabel("Date:")).setBounds(30, 70, 100, 30);
                editFrame.add(dateField);
                editFrame.add(new JLabel("Time:")).setBounds(30, 110, 100, 30);
                editFrame.add(timeField);
                editFrame.add(new JLabel("Event Type:")).setBounds(30, 150, 100, 30);
                editFrame.add(eventTypeField);
                editFrame.add(new JLabel("Guest Count:")).setBounds(30, 190, 100, 30);
                editFrame.add(guestCountField);
                editFrame.add(new JLabel("Place:")).setBounds(30, 230, 100, 30);
                editFrame.add(placeField);

                JButton updateButton = new JButton("Update Reservation");
                updateButton.setBounds(150, 280, 150, 30);
                updateButton.addActionListener(e -> {
                    updateReservation(reservationId, clientIdField.getText(), dateField.getText(), timeField.getText(),
                            eventTypeField.getText(), guestCountField.getText(), placeField.getText());
                    editFrame.dispose();
                });
                editFrame.add(updateButton);

                editFrame.setLocationRelativeTo(null);
                editFrame.setVisible(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching reservation details: " + e.getMessage());
        }
    }

    private void updateReservation(int reservationId, String clientId, String date, String time, String eventType,
            String guestCount, String place) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE Reservations SET client_id = ?, reservation_date = ?, reservation_time = ?, event_type = ?, guest_count = ?, place = ? WHERE reservation_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, Integer.parseInt(clientId));
            statement.setString(2, date);
            statement.setString(3, time);
            statement.setString(4, eventType);
            statement.setInt(5, Integer.parseInt(guestCount));
            statement.setString(6, place);
            statement.setInt(7, reservationId);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Reservation updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating reservation: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new AdminDashboard();
    }
}