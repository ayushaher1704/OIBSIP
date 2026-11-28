import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class MainFrame extends JFrame {
    // Booking tab fields
    private final JTextField passengerNameField = new JTextField(22);
    private final JComboBox<String> trainComboBox = new JComboBox<>();
    private final JComboBox<String> boardingComboBox = new JComboBox<>();
    private final JComboBox<String> destinationComboBox = new JComboBox<>();
    private final JComboBox<String> classComboBox = new JComboBox<>(new String[]{
        "1A - First AC",
        "2A - AC 2 Tier",
        "3A - AC 3 Tier",
        "SL - Sleeper",
        "CC - Chair Car",
        "EC - Executive Chair Car"
    });
    private final JTextField journeyDateField = new JTextField(13);

    // Cancellation tab fields
    private final JTextField pnrSearchField = new JTextField(16);
    private final JLabel cancelPnrValue = new JLabel("-");
    private final JLabel cancelPassengerValue = new JLabel("-");
    private final JLabel cancelTrainValue = new JLabel("-");
    private final JLabel cancelClassValue = new JLabel("-");
    private final JLabel cancelDateValue = new JLabel("-");
    private final JLabel cancelRouteValue = new JLabel("-");
    private final JButton confirmCancelButton = new JButton("Confirm Cancellation");
    private String loadedPnr = null;

    public MainFrame(String clerkName) {
        setTitle("Railway Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        add(createTopBar(clerkName), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Book Ticket", createBookingPanel());
        tabbedPane.addTab("Cancel Ticket", createCancellationPanel());

        add(tabbedPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createTopBar(String clerkName) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(215, 220, 228)),
            new EmptyBorder(10, 18, 10, 18)
        ));

        JLabel userLabel = new JLabel("Signed in as: " + clerkName);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton signOutButton = new JButton("Sign Out");
        signOutButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        signOutButton.setFocusPainted(false);
        signOutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        topBar.add(userLabel, BorderLayout.WEST);
        topBar.add(signOutButton, BorderLayout.EAST);
        return topBar;
    }

    // ==================== TAB 1: BOOK TICKET ====================
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 26, 20, 26));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("New Ticket Reservation");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(heading, gbc);

        List<String> trains = ReservationService.getAllTrains();
        for (String train : trains) {
            trainComboBox.addItem(train);
        }
        trainComboBox.setMaximumRowCount(8);

        journeyDateField.setEditable(false);
        journeyDateField.setBackground(Color.WHITE);
        JButton pickDateButton = new JButton("Select Date");
        pickDateButton.setFocusPainted(false);
        pickDateButton.addActionListener(e -> {
            String chosenDate = DatePickerDialog.pickDate(this, journeyDateField.getText());
            if (chosenDate != null) {
                journeyDateField.setText(chosenDate);
            }
        });

        JPanel datePanel = new JPanel(new BorderLayout(6, 0));
        datePanel.add(journeyDateField, BorderLayout.CENTER);
        datePanel.add(pickDateButton, BorderLayout.EAST);

        gbc.gridwidth = 1;
        addRow(panel, gbc, 1, "Passenger Name:", passengerNameField);
        addRow(panel, gbc, 2, "Select Train:", trainComboBox);
        addRow(panel, gbc, 3, "Boarding Station:", boardingComboBox);
        addRow(panel, gbc, 4, "Destination Station:", destinationComboBox);
        addRow(panel, gbc, 5, "Class Type:", classComboBox);
        addRow(panel, gbc, 6, "Journey Date:", datePanel);

        trainComboBox.addActionListener(e -> updateRouteStations());
        updateRouteStations();

        JButton bookButton = new JButton("Book Ticket");
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 4, 6);
        panel.add(bookButton, gbc);

        bookButton.addActionListener(e -> handleBooking());

        return panel;
    }

    // ==================== TAB 2: CANCEL TICKET ====================
    private JPanel createCancellationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 26, 20, 26));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Cancel Reservation by PNR");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(heading, gbc);

        JButton fetchButton = new JButton("Fetch Ticket");
        fetchButton.setFocusPainted(false);
        fetchButton.addActionListener(e -> handleFetchPnr());

        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.add(pnrSearchField, BorderLayout.CENTER);
        searchPanel.add(fetchButton, BorderLayout.EAST);

        gbc.gridwidth = 1;
        addRow(panel, gbc, 1, "Enter PNR Number:", searchPanel);

        JPanel detailsBox = new JPanel(new GridBagLayout());
        detailsBox.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Ticket Details"),
            new EmptyBorder(8, 12, 8, 12)
        ));
        GridBagConstraints dGbc = new GridBagConstraints();
        dGbc.insets = new Insets(5, 6, 5, 12);
        dGbc.anchor = GridBagConstraints.WEST;
        dGbc.fill = GridBagConstraints.HORIZONTAL;

        addDetailRow(detailsBox, dGbc, 0, "PNR Number:", cancelPnrValue);
        addDetailRow(detailsBox, dGbc, 1, "Passenger Name:", cancelPassengerValue);
        addDetailRow(detailsBox, dGbc, 2, "Train:", cancelTrainValue);
        addDetailRow(detailsBox, dGbc, 3, "Class:", cancelClassValue);
        addDetailRow(detailsBox, dGbc, 4, "Journey Date:", cancelDateValue);
        addDetailRow(detailsBox, dGbc, 5, "Route:", cancelRouteValue);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 8, 6);
        panel.add(detailsBox, gbc);

        confirmCancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmCancelButton.setEnabled(false);
        confirmCancelButton.addActionListener(e -> handleCancelTicket());

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 6, 4, 6);
        panel.add(confirmCancelButton, gbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JLabel valueLabel) {
        JLabel keyLabel = new JLabel(labelText);
        keyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        panel.add(keyLabel, gbc);

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(valueLabel, gbc);
    }

    private void updateRouteStations() {
        String selectedTrain = (String) trainComboBox.getSelectedItem();
        if (selectedTrain == null) return;

        String trainNumber = selectedTrain.split(" - ", 2)[0].trim();
        String[] stops = ReservationService.getRouteStops(trainNumber);

        boardingComboBox.removeAllItems();
        destinationComboBox.removeAllItems();

        for (String stop : stops) {
            boardingComboBox.addItem(stop);
            destinationComboBox.addItem(stop);
        }

        if (stops.length > 1) {
            boardingComboBox.setSelectedIndex(0);
            destinationComboBox.setSelectedIndex(stops.length - 1);
        }
    }

    private void handleBooking() {
        String passenger = passengerNameField.getText();
        String selectedTrain = (String) trainComboBox.getSelectedItem();
        String boarding = (String) boardingComboBox.getSelectedItem();
        String destination = (String) destinationComboBox.getSelectedItem();
        String classType = (String) classComboBox.getSelectedItem();
        String date = journeyDateField.getText();

        if (selectedTrain == null || boarding == null || destination == null) {
            JOptionPane.showMessageDialog(this, "Please select a train and route.", "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (boardingComboBox.getSelectedIndex() >= destinationComboBox.getSelectedIndex()) {
            JOptionPane.showMessageDialog(
                this,
                "Destination station must come after the boarding station on this train's route.",
                "Invalid Route Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String[] trainParts = selectedTrain.split(" - ", 2);
        String trainNumber = trainParts[0].trim();
        String trainName = trainParts[1].trim();

        try {
            Reservation ticket = ReservationService.bookTicket(
                passenger, trainNumber, trainName, classType, date, boarding, destination
            );

            String receipt = String.format(
                "Reservation Confirmed!\n\n" +
                "PNR Number:    %s\n" +
                "Passenger:     %s\n" +
                "Train:         %s - %s\n" +
                "Class:         %s\n" +
                "Journey Date:  %s\n" +
                "Route:         %s  ->  %s",
                ticket.getPnr(),
                ticket.getPassengerName(),
                ticket.getTrainNumber(),
                ticket.getTrainName(),
                ticket.getClassType(),
                ticket.getJourneyDate(),
                ticket.getSource(),
                ticket.getDestination()
            );

            JOptionPane.showMessageDialog(
                this,
                receipt,
                "Booking Receipt — PNR " + ticket.getPnr(),
                JOptionPane.INFORMATION_MESSAGE
            );

            passengerNameField.setText("");
            journeyDateField.setText("");
            classComboBox.setSelectedIndex(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Cannot Complete Booking",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void handleFetchPnr() {
        String pnrInput = pnrSearchField.getText().trim();

        if (pnrInput.isEmpty() || !pnrInput.matches("\\d+")) {
            clearCancellationDetails();
            JOptionPane.showMessageDialog(
                this,
                "Please enter a valid numeric PNR number.",
                "Invalid PNR",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Reservation ticket = ReservationService.findByPnr(pnrInput);
        if (ticket == null) {
            clearCancellationDetails();
            JOptionPane.showMessageDialog(
                this,
                "No active reservation found for PNR: " + pnrInput,
                "PNR Not Found",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        loadedPnr = ticket.getPnr();
        cancelPnrValue.setText(ticket.getPnr());
        cancelPassengerValue.setText(ticket.getPassengerName());
        cancelTrainValue.setText(ticket.getTrainNumber() + " - " + ticket.getTrainName());
        cancelClassValue.setText(ticket.getClassType());
        cancelDateValue.setText(ticket.getJourneyDate());
        cancelRouteValue.setText(ticket.getSource() + "  ->  " + ticket.getDestination());
        confirmCancelButton.setEnabled(true);
    }

    private void handleCancelTicket() {
        if (loadedPnr == null) return;

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel PNR " + loadedPnr + " for " + cancelPassengerValue.getText() + "?\nThis action cannot be undone.",
            "Confirm Ticket Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            boolean deleted = ReservationService.cancelByPnr(loadedPnr);
            if (deleted) {
                JOptionPane.showMessageDialog(
                    this,
                    "Reservation PNR " + loadedPnr + " has been cancelled.",
                    "Ticket Cancelled",
                    JOptionPane.INFORMATION_MESSAGE
                );
                pnrSearchField.setText("");
                clearCancellationDetails();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not cancel reservation. It may have already been removed.",
                    "Cancellation Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void clearCancellationDetails() {
        loadedPnr = null;
        cancelPnrValue.setText("-");
        cancelPassengerValue.setText("-");
        cancelTrainValue.setText("-");
        cancelClassValue.setText("-");
        cancelDateValue.setText("-");
        cancelRouteValue.setText("-");
        confirmCancelButton.setEnabled(false);
    }
}