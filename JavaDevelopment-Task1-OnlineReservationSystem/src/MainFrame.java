import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
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

    public MainFrame(String clerkName) {
        setTitle("Railway Reservation System — Logged in as " + clerkName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Book Ticket", createBookingPanel());

        add(tabbedPane);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 26, 20, 26));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Heading
        JLabel heading = new JLabel("New Ticket Reservation");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(heading, gbc);

        // Load all 12 trains into the scrollable dropdown
        List<String> trains = ReservationService.getAllTrains();
        for (String train : trains) {
            trainComboBox.addItem(train);
        }
        trainComboBox.setMaximumRowCount(8);

        // Date field + "Select Date" calendar trigger button
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

        // Add form rows
        gbc.gridwidth = 1;
        addRow(panel, gbc, 1, "Passenger Name:", passengerNameField);
        addRow(panel, gbc, 2, "Select Train:", trainComboBox);
        addRow(panel, gbc, 3, "Boarding Station:", boardingComboBox);
        addRow(panel, gbc, 4, "Destination Station:", destinationComboBox);
        addRow(panel, gbc, 5, "Class Type:", classComboBox);
        addRow(panel, gbc, 6, "Journey Date:", datePanel);

        // Refresh boarding & destination stops whenever a different train is chosen
        trainComboBox.addActionListener(e -> updateRouteStations());
        updateRouteStations(); // Populate stations for the initial train immediately

        // Book Ticket Button
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

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void updateRouteStations() {
        String selectedTrain = (String) trainComboBox.getSelectedItem();
        if (selectedTrain == null) return;

        // Split only on the first " - " so train names with hyphens stay intact
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

            // Reset fields for the next booking
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

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainFrame("Ayush Aher").setVisible(true));
    }
}